package com.zhsaidk.http.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/projects")
public class ProjectController {

    @GetMapping
    public String getProjects(){
        return "project/projects";
    }

    @GetMapping("/{id}")
    public String getProject(){
        return "project/project";
    }

    @GetMapping("/build")
    public String getProjectCreate(){
        return "project/build";
    }

    @GetMapping("/catalogs")
    public String getCatalogs(){
        return "catalog/catalogs";
    }

    @GetMapping("/catalogs/{id}")
    public String getCatalogBuild(){
        return "catalog/catalog";
    }

    @GetMapping("/catalogs/build")
    public String getCatalogsBuild(){
        return "catalog/build";
    }

    @GetMapping("/catalogs/events")
    public String getEvents(){
        return "event/events";
    }

    @GetMapping("/catalogs/events/{id}")
    public String getEventById(){
        return "event/event";
    }

    @GetMapping("/catalogs/events/build")
    public String getEventBuild(){
        return "event/build";
    }
}
