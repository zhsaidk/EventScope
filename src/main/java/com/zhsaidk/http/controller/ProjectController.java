package com.zhsaidk.http.controller;

import com.zhsaidk.service.CatalogService;
import com.zhsaidk.service.EventService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@Controller
@RequiredArgsConstructor
public class ProjectController {

    private final CatalogService catalogService;
    private final EventService eventService;

    @GetMapping("/projects")
    public String getProjects(){
        return "project/projects";
    }

    @GetMapping("/projects/build")
    public String getProjectCreate(){
        return "project/build";
    }

    @GetMapping("/projects/catalogs")
    public String getCatalogs(){
        return "catalog/catalogs";
    }


    @GetMapping("/projects/catalogs/build")
    public String getCatalogsBuild(){
        return "catalog/build";
    }

    @GetMapping("/projects/catalogs/events")
    public String getEvents(){
        return "event/events";
    }

    @GetMapping("/projects/catalogs/events/build")
    public String getEventBuild(){
        return "event/build";
    }

    @GetMapping("projects/catalogs/{projectSlug}")
    public String getCatalogWithProjectSlug(@PathVariable("projectSlug") String projectSlug,
                             Model model){
        model.addAttribute("catalogs", catalogService.findAllCatalogsByProjectSlug(projectSlug));
        return "catalog/catalogsWithProjectSlug";
    }

    @GetMapping("projects/catalogs/events/{catalogSlug}")
    public String getEventsWithCatalogSlug(@PathVariable("catalogSlug") String catalogSlug,
                             Model model){
        model.addAttribute("events", eventService.findAllEventsByCatalogSlug(catalogSlug));
        return "event/eventsWithCatalogSlug";
    }

    @GetMapping("/login")
    public String loginPage(){
        return "user/login";
    }
}
