package com.intrasoft.csp.server.processors;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.intrasoft.csp.commons.exceptions.CspBusinessException;
import com.intrasoft.csp.commons.model.*;
import com.intrasoft.csp.commons.routes.CamelRoutes;
import com.intrasoft.csp.server.service.CamelRestService;
import com.intrasoft.csp.server.routes.RouteUtils;
import org.apache.camel.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.util.*;

/**
 * Created by iskitsas on 4/9/17.
 */
@Component
public class TcProcessor implements Processor,CamelRoutes{
    private static final Logger LOG = LoggerFactory.getLogger(TcProcessor.class);

    @Value("${server.name}")
    String serverName;
    @Value("${tc.protocol}")
    String tcProtocol;
    @Value("${tc.host}")
    String tcHost;
    @Value("${tc.port}")
    String tcPort;
    @Value("${tc.path.circles}")
    String tcPathCircles;
    @Value("${tc.path.teams}")
    String tcPathTeams;

    @Autowired
    ObjectMapper objectMapper;

    @Autowired
    CamelRestService camelRestService;

    @Autowired
    ProducerTemplate producer;


    @Autowired
    RouteUtils routes;

    @PostConstruct
    public void init(){
        LOG.info("This CSP server name is: "+serverName);
    }

    @Override
    public void process(Exchange exchange) throws Exception {
        String originEndpoint = (String) exchange.getIn().getHeader(CamelRoutes.ORIGIN_ENDPOINT);
        String msg = originEndpoint.equals(routes.apply(CamelRoutes.DCL))? "send to external CSP":
                originEndpoint.equals(routes.apply(CamelRoutes.EDCL))? " handle from external CSP":"";
        LOG.info("DCL - Get Trust Circles from TC API and "+msg+" [ORIGIN_ENDPOINT:"+originEndpoint+"]");

        IntegrationData integrationData = exchange.getIn().getBody(IntegrationData.class);
        String httpMethod = (String) exchange.getIn().getHeader(Exchange.HTTP_METHOD);

        //make all TC calls

        String uri = null;
        String getAllTcUri = this.getTcCirclesURI();
        List<TrustCircle> tcList = camelRestService.sendAndGetList(getAllTcUri, null,  HttpMethod.GET.name(), TrustCircle.class,null);

        Optional<TrustCircle> optionalTc  = tcList.stream().filter(t->t.getShortName().toLowerCase().contains(IntegrationDataType.tcNamingConventionForShortName.get(integrationData.getDataType()).toString().toLowerCase())).findAny();
        if(optionalTc.isPresent()){
            uri = this.getTcCirclesURI() + "/" + optionalTc.get().getId();
        }else{
            throw new CspBusinessException("Could not find trust circle id for this data. "+integrationData.toString());
        }

        TrustCircle tc = camelRestService.send(uri, null,  HttpMethod.GET.name(), TrustCircle.class);

        List<Team> teams = new ArrayList<>();
        //first make all calls to get the teams
        for (String teamId : tc.getTeams()){
            //make call to TC-team
            Team team = camelRestService.send(this.getTcTeamsURI() + "/" + teamId, teamId, HttpMethod.GET.name(), Team.class);
            if(team.getShortName()==null){
                throw new CspBusinessException("Team short name received from TC API is null - cannot proceed. \n" +
                        "TrustCircle: "+tc.toString()+"\n" +
                        "Team: "+team.toString());
            }

            if (!team.getShortName().equals(serverName)){
                teams.add(team);
            }
        }
        //all TC calls have been made up to this point, TEAMS list has been populated

        // Decide the flow
        if(originEndpoint.equals(routes.apply(CamelRoutes.DCL))) {
            for(Team team:teams) {
                //send to ECSP
                LOG.info(team.toString());
                LOG.info(integrationData.toString());
                handleDclFlowAndSendToECSP(httpMethod, team, integrationData);
            }
        }else if(originEndpoint.equals(routes.apply(CamelRoutes.EDCL))){
            handleExternalDclFlowAndSendToDSL(exchange,httpMethod, teams, integrationData);
        }
    }

    private void handleDclFlowAndSendToECSP(String httpMethod, Team team, IntegrationData integrationData){
        EnhancedTeamDTO enhancedTeamDTO = new EnhancedTeamDTO(team, integrationData);
        Map<String, Object> headers = new HashMap<>();

        headers.put(Exchange.HTTP_METHOD, httpMethod);
        producer.sendBodyAndHeaders(routes.apply(ECSP), ExchangePattern.InOut, enhancedTeamDTO, headers);
    }

    private void handleExternalDclFlowAndSendToDSL(Exchange exchange,String httpMethod,List<Team> teams, IntegrationData integrationData){

        boolean authorized = teams.stream().anyMatch(t->t.getShortName().toLowerCase().equals(integrationData.getDataParams().getCspId().toLowerCase()));
        LOG.info("Authorized (cspId or shortName="+integrationData.getDataParams().getCspId().toLowerCase()+"): "+authorized);
        if (authorized){
            integrationData.getSharingParams().setIsExternal(true);

            //exchange.getIn().setBody(integrationData); //replace with producer
            //exchange.getIn().setHeader("recipients", routes.apply(DSL));//replace with producer
            Map<String, Object> headers = new HashMap<>();
            headers.put(Exchange.HTTP_METHOD, httpMethod);
            producer.sendBodyAndHeaders(routes.apply(DSL),ExchangePattern.InOut,integrationData,headers);
        }
    }

    private String getTcCirclesURI() {
        return tcProtocol + "://" + tcHost + ":" + tcPort + tcPathCircles;
    }
    private String getTcTeamsURI() {
        return tcProtocol + "://" + tcHost + ":" + tcPort + tcPathTeams;
    }
}