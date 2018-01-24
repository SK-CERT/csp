package com.intrasoft.csp.misp.tests.sandbox;

import com.intrasoft.csp.client.TrustCirclesClient;
import com.intrasoft.csp.client.config.TrustCirclesClientConfig;
import com.intrasoft.csp.libraries.restclient.service.RetryRestTemplate;
import com.intrasoft.csp.misp.client.MispAppClient;
import com.intrasoft.csp.misp.client.config.MispAppClientConfig;
import com.intrasoft.csp.misp.commons.models.generated.SharingGroup;
import com.intrasoft.csp.misp.service.MispTcSyncService;
import com.intrasoft.csp.misp.service.impl.MispTcSyncServiceImpl;
import com.intrasoft.csp.misp.tests.sandbox.util.MispMockUtil;
import org.apache.commons.io.FileUtils;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.test.web.client.response.MockRestResponseCreators;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.charset.Charset;
import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertTrue;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;


@RunWith(SpringJUnit4ClassRunner.class)

@SpringBootTest( classes = {MispTcSyncServiceImpl.class, TrustCirclesClient.class,
                            TrustCirclesClientConfig.class, MispAppClient.class, MispAppClientConfig.class},
        properties = {
                "misp.app.protocol:http",
                "misp.app.host:192.168.56.50",
                "misp.app.port:80",
                "misp.app.authorization.key:JNqWBxfPiIywz7hUe58MyJf6sD5PrTVaGm7hTn6c",
                "spring.jackson.deserialization.unwrap-root-value=true"
        }
)

public class MispTcSyncServiceTest {

    private static final Logger LOG = LoggerFactory.getLogger(MispTcSyncService.class);

    @Autowired
    MispTcSyncService mispTcSyncService;

    @Autowired
//  @Qualifier("MispAppClient")
    MispAppClient mispAppClient;

    @Autowired
    TrustCirclesClientConfig tcConfig;

    @Autowired
    @Qualifier("TcRestTemplate")
    RetryRestTemplate tcRetryRestTemplate;

    @Autowired
    @Qualifier("MispAppRestTemplate")
    RetryRestTemplate mispRetryRestTemplate;

    // Necessary mock response files
    URL allTeams = getClass().getClassLoader().getResource("json/allTeams.json");
    URL twoTeams = getClass().getClassLoader().getResource("json/twoTeams.json");
    URL allTrustCircles = getClass().getClassLoader().getResource("json/allTrustCircles.json");
    URL allLocalTrustCircles = getClass().getClassLoader().getResource("json/allLocalTrustCircles.json");
    URL twoTrustCircles = getClass().getClassLoader().getResource("json/twoTrustCircles.json");
    URL sharingGroup = getClass().getClassLoader().getResource("json/sharingGroup.json");
    URL allSharingGroups = getClass().getClassLoader().getResource("json/allSharingGroups.json");
    URL organisation = getClass().getClassLoader().getResource("json/organisation.json");

    // Service should synchronize Trust Circles' Teams with MISP's Organisations.
    // TODO: Organisations in MISP can't be deleted when tied with users or events. UI Response Message:
    // "Organisation could not be deleted. Generally organisations should never be deleted, instead consider moving them
    // to the known remote organisations list. Alternatively, if you are certain that you would like to remove an
    // organisation and are aware of the impact, make sure that there are no users or events still tied to this
    // organisation before deleting it."
    // Given 6 TC Teams, MISP should create them if they don't already exist.
    @Test
    public void syncOrganisationsShouldAddSixReturnEightTest() throws URISyntaxException, IOException {

        //mock the TC server using json retrieved from a real TC (6 teams in file)

        String apiUrl = tcConfig.getTcTeamsURI();
        MockRestServiceServer mockServer = MockRestServiceServer.bindTo(tcRetryRestTemplate).build();
        mockServer.expect(requestTo(apiUrl))
                .andRespond(MockRestResponseCreators
                        .withSuccess(FileUtils.readFileToString(new File(allTeams.toURI()), Charset.forName("UTF-8"))
                                .getBytes(), MediaType.APPLICATION_JSON_UTF8));

        mispTcSyncService.syncOrganisations();

        // TODO: Remove plus 2 when synchronizing implementation supports deletion for organisations tied to events/users
        assertThat(mispAppClient.getAllMispOrganisations().size(), is(6+2));
        mockServer.verify();
    }
    // When two TC teams exist, those two MISP organisations should only be left in MISP after synchronizing.
    @Test
    public void syncOrganisationsGivenTwoTeamsReturnTwoOrganisationsTest() throws URISyntaxException, IOException {

        String apiUrl = tcConfig.getTcTeamsURI();
        MockRestServiceServer mockServer = MockRestServiceServer.bindTo(tcRetryRestTemplate).build();
        mockServer.expect(requestTo(apiUrl))
                .andRespond(MockRestResponseCreators
                        .withSuccess(FileUtils.readFileToString(new File(twoTeams.toURI()), Charset.forName("UTF-8"))
                                .getBytes(), MediaType.APPLICATION_JSON_UTF8));

        mispTcSyncService.syncOrganisations();

        assertThat(mispAppClient.getAllMispOrganisations().size(), is(2));
        mockServer.verify();

    }
    //This is the scenario where TC' Trust Circles don't already exist in MISP and need to be created.
    //Mocking a response where TC returns two Trust Circles and MISP creates them as Sharing Groups if they don't exist.
    @Test
    public void syncSharingGroupsNonExistingSharingGroupsTest() throws URISyntaxException, IOException {
        String tcCirclesURI = tcConfig.getTcCirclesURI();
        String mispGroupsURI = "http://192.168.56.50:80/sharing_groups";
        String mispAddGroupURI = "http://192.168.56.50:80/admin/sharing_groups/add";
        String sharingGroupUuid = "a36c31f4-dad3-4f49-b443-e6d6333649b1";

        // We first need to mock TC server's getAllTrustCircles response
        MockRestServiceServer tcMockServer = MockRestServiceServer.bindTo(tcRetryRestTemplate).build();
        tcMockServer.expect(requestTo(tcCirclesURI))
                .andRespond(MockRestResponseCreators
                        .withSuccess(FileUtils.readFileToString(new File(twoTrustCircles.toURI()),
                                Charset.forName("UTF-8")).getBytes(), MediaType.APPLICATION_JSON_UTF8));
        // then MISP's getAllSharingGroups response
        MockRestServiceServer mispMockServer = MockRestServiceServer.bindTo(mispRetryRestTemplate).build();
        mispMockServer.expect(requestTo(mispGroupsURI))
                .andRespond(MockRestResponseCreators
                        .withSuccess(FileUtils.readFileToString(new File(allSharingGroups.toURI()),
                                Charset.forName("UTF-8")).getBytes(), MediaType.APPLICATION_JSON_UTF8));
        // then MISP's first addSharingGroup response (first mock tc doesn't have any teams; adding sg straight away)
        mispMockServer.expect(requestTo(mispAddGroupURI))
                .andRespond(MockRestResponseCreators
                        .withSuccess(FileUtils.readFileToString(new File(sharingGroup.toURI()),
                                Charset.forName("UTF-8")).getBytes(), MediaType.APPLICATION_JSON_UTF8));
        // then MISP's first getOrganisation call (sg sync mechanism gets organisation objects by uuid)
        mispMockServer.expect(requestTo("http://192.168.56.50:80/organisations/view/578c0e4e-ebaf-455b-a2a1-faffb14be9e1"))
                .andRespond(MockRestResponseCreators
                        .withSuccess(FileUtils.readFileToString(new File(organisation.toURI()),
                                Charset.forName("UTF-8")).getBytes(), MediaType.APPLICATION_JSON_UTF8));
        // then MISP's 2nd getOrganisation call (the 2nd organisation of the 2nd sharing group)
        mispMockServer.expect(requestTo("http://192.168.56.50:80/organisations/view/af9d06ac-d7be-4684-86a3-808fe4f4d17c"))
                .andRespond(MockRestResponseCreators
                        .withSuccess(FileUtils.readFileToString(new File(organisation.toURI()),
                                Charset.forName("UTF-8")).getBytes(), MediaType.APPLICATION_JSON_UTF8));

        // finally mocking MISP server's response for creating the 2nd sharing group in MISP
        mispMockServer.expect(requestTo(mispAddGroupURI))
                .andRespond(MockRestResponseCreators
                        .withSuccess(MispMockUtil.getJsonBytesForSharingGroupByUuid(allSharingGroups, sharingGroupUuid),
                                MediaType.APPLICATION_JSON_UTF8));


//        TODO: Fix rejection of further requests and write assertions before moving to scenario b
        mispTcSyncService.syncSharingGroups();

        tcMockServer.verify();
        mispMockServer.verify();
    }

    @Test
    public void syncSharingGroupsLocalTrustCirclesTest() throws URISyntaxException, IOException {

        String tcCirclesURI = tcConfig.getTcCirclesURI();
        String tcLocalCirclesURI = tcConfig.getTcLocalCirclesURI();
        String mispGroupsURI = "http://192.168.56.50:80/sharing_groups";

        // We first need to mock TC server's getAllTrustCircles response
        MockRestServiceServer tcMockServer = MockRestServiceServer.bindTo(tcRetryRestTemplate).build();
        tcMockServer.expect(requestTo(tcCirclesURI))
                .andRespond(MockRestResponseCreators
                        .withSuccess(FileUtils.readFileToString(new File(twoTrustCircles.toURI()),
                                Charset.forName("UTF-8")).getBytes(), MediaType.APPLICATION_JSON_UTF8));

        // Then mock TC server's getAllLocalTrustCircles response
        tcMockServer.expect(requestTo(tcLocalCirclesURI))
                .andRespond(MockRestResponseCreators
                        .withSuccess(FileUtils.readFileToString(new File(allLocalTrustCircles.toURI()),
                                Charset.forName("UTF-8")).getBytes(), MediaType.APPLICATION_JSON_UTF8));

        mispTcSyncService.syncSharingGroups();

        // assert ltcs are now on misp as sharing groups
        String[] uuids = {"7703853d-1c32-4556-b34b-c666f212cdc9", "31146113-d53d-4738-877d-2405ea18edf8", "5b2af720-e192-4cd5-8e5d-db3181c8a475"};
        String[] names = {"LTC shortname A", "LTC shortname B", "LTC shortname C"};

        // assert uuid and name for ltcA
        SharingGroup sharingGroupA = mispAppClient.getMispSharingGroup(uuids[0]);
        assertThat(sharingGroupA.getUuid(), is(uuids[0]));
        assertThat(sharingGroupA.getName(), is(names[0]));

        // assert uuid and name for ltcB
        SharingGroup sharingGroupB = mispAppClient.getMispSharingGroup(uuids[1]);
        assertThat(sharingGroupB.getUuid(), is(uuids[1]));
        assertThat(sharingGroupB.getName(), is(names[1]));
        // assert uuid and name for ltcC
        SharingGroup sharingGroupC = mispAppClient.getMispSharingGroup(uuids[2]);
        assertThat(sharingGroupC.getUuid(), is(uuids[2]));
        assertThat(sharingGroupC.getName(), is(names[2]));

        tcMockServer.verify();
    }

    ////This is the scenario where some of the TC' Trust Circles already exist in MISP and need to be updated.
    @Test
    public void syncSharingGroupsExistingSharingGroupsTest() {

    }

    //Description
    @Test
    public void syncAllScenarioATest() {

    }
    //Description
    @Test
    public void syncAllScenarioBTest() {

    }


}
