package com.intrasoft.csp.anon.server.controller;

import com.intrasoft.csp.anon.commons.model.RuleSetDTO;
import com.intrasoft.csp.anon.server.model.RuleSet;
import com.intrasoft.csp.anon.server.service.AnonService;
import org.hibernate.exception.ConstraintViolationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

/**
 * Created by chris on 26/6/2017.
 */

@Controller
public class RuleSetController {

    private static final Logger LOG = LoggerFactory.getLogger(RuleSetController.class);

    @Autowired
    AnonService anonService;

    @GetMapping("/rulesets")
    public String showRulesets(RuleSet ruleset, Model model) {
        LOG.info("UI: GET rulesets ");
        List<RuleSetDTO> rulesets =anonService.getAllRuleSet();
        model.addAttribute("rulesets", rulesets);
        model.addAttribute("ruleset", ruleset);
        return "pages/rulesets";
    }

    @PostMapping("/rulesets/save")
    public String addRuleset(@RequestPart(value = "file", required = false) MultipartFile file,
                             RedirectAttributes redirectAttributes,
                             @ModelAttribute RuleSetDTO ruleset,
                             BindingResult result,
                             Model model) throws IOException {
        LOG.info("UI: SAVE ruleset " + ruleset.toString());
        model.addAttribute("description", ruleset.getDescription());
        ruleset.setFilename(file.getOriginalFilename());
        ruleset.setFile(file.getBytes());
        if (file.isEmpty()) {
            redirectAttributes.addFlashAttribute("error", "Please select a file to upload");
            return "redirect:";
        }
        try {
            byte[] bytes = file.getBytes();
            Path path = Paths.get(file.getOriginalFilename());
            ruleset.setFile(bytes);
            ruleset.setFilename(file.getOriginalFilename());
        } catch (IOException e) {
            LOG.error("File upload failed: " + e);
            redirectAttributes.addFlashAttribute("error", "Something went wrong");
            return "redirect:";
        }

        ruleset = anonService.saveRuleSet(ruleset);
        List<RuleSetDTO> rulesets =anonService.getAllRuleSet();
        model.addAttribute("rulesets", rulesets);
        redirectAttributes.addFlashAttribute("msg", "Ruleset imported.");
        return "redirect:";
    }

    @GetMapping("/rulesets/{id}")
    public ModelAndView showRuleset(@PathVariable Long id) {
        LOG.info("UI: GET ruleset with id" + id);
        ModelAndView mav = new ModelAndView("pages/rulesets");
        mav.addObject("rulesets", anonService.getAllRuleSet());
        RuleSetDTO ruleset = anonService.getRuleSetById(id);
        mav.addObject("ruleset", ruleset);
        return mav;
    }

    @GetMapping("/ruleset/delete/{id}")
    public ModelAndView deleteMapping(@PathVariable Long id, RedirectAttributes redirect) throws ConstraintViolationException {
        LOG.info("UI: DELETE ruleset with id: " + id);
        anonService.deleteRuleSet(id);
        ModelAndView mav = new ModelAndView("redirect:/rulesets");
        mav.addObject("mappings",anonService.getAllRuleSet());
        redirect.addFlashAttribute("msg", "Ruleset deleted");
        return mav;
    }

    @ExceptionHandler(ConstraintViolationException.class)
    public ModelAndView handleSqlException(HttpServletRequest request, Exception ex, RedirectAttributes redirect){
        LOG.error("Requested URL="+request.getRequestURL());
        LOG.error("ConstraintViolationException Raised");
        ModelAndView mav = new ModelAndView("redirect:/rulesets");
        mav.addObject("exception", ex);
        mav.addObject("url", request.getRequestURL());
        mav.addObject("mappings",anonService.getAllRuleSet());
        redirect.addFlashAttribute("error", "Ruleset could not be deleted");
        return mav;
    }

}
