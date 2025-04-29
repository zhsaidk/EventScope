package com.zhsaidk.http.controller;

import com.zhsaidk.database.entity.Catalog;
import com.zhsaidk.database.entity.Event;
import com.zhsaidk.database.entity.Project;
import com.zhsaidk.database.repo.EventSpecification;
import com.zhsaidk.service.CatalogService;
import com.zhsaidk.service.EventService;
import com.zhsaidk.service.ProjectService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.time.LocalDateTime;

@Controller
@RequiredArgsConstructor
public class ProjectController {

    private final CatalogService catalogService;
    private final EventService eventService;
    private final ProjectService projectService;

    @GetMapping("/projects")
    public String getProjects(Model model,
                              @RequestParam(value = "page", defaultValue = "0") Integer page,
                              @RequestParam(value = "size", defaultValue = "10") Integer size) {
        PageRequest pageRequest = PageRequest.of(page, size, Sort.by("createdAt"));
        Page<Project> projectPage = projectService.getAllProjects(pageRequest);

        model.addAttribute("projects", projectPage.getContent());
        model.addAttribute("currentPage", projectPage.getNumber());
        model.addAttribute("totalPages", projectPage.getTotalPages());
        model.addAttribute("totalItems", projectPage.getTotalElements());
        model.addAttribute("pageSize", size);

        return "project/projects";
    }

    @PostMapping("/projects/delete/{slug}")
    public String deleteProject(@PathVariable String slug) {
        projectService.remove(slug);
        return "redirect:/projects";
    }

    @GetMapping("/projects/build")
    public String getProjectCreate(){
        return "project/build";
    }

    @GetMapping("/projects/catalogs")
    public String getCatalogs(Model model,
                              @RequestParam(value = "page", defaultValue = "0") Integer page,
                              @RequestParam(value = "size", defaultValue = "10") Integer size){
        Page<Catalog> catalogs = catalogService.findAllCatalogs(PageRequest.of(page, size, Sort.by("createdAt").ascending()));
        model.addAttribute("catalogs", catalogs.getContent());
        model.addAttribute("currentPage", catalogs.getNumber());
        model.addAttribute("totalPages", catalogs.getTotalPages());
        model.addAttribute("totalItems", catalogs.getTotalElements());
        model.addAttribute("pageSize", size);
        return "catalog/catalogs";
    }


    @GetMapping("/projects/catalogs/build")
    public String getCatalogsBuild(){
        return "catalog/build";
    }

    @GetMapping("/projects/catalogs/events")
    public String getEvents(Model model,
                            @RequestParam(value = "page", defaultValue = "0") Integer page,
                            @RequestParam(value = "size", defaultValue = "10") Integer size,
                            @RequestParam(value = "text", required = false) String text,
                            @RequestParam(value = "begin", required = false) LocalDateTime begin,
                            @RequestParam(value = "end", required = false) LocalDateTime end,
                            @RequestParam(value = "showCatalogId", required = false) Long showCatalogId) {
        PageRequest pageRequest = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        Page<Event> events = eventService.findAllEvents(pageRequest, text, begin, end);

        model.addAttribute("events", events.getContent());
        model.addAttribute("currentPage", events.getNumber());
        model.addAttribute("totalPages", events.getTotalPages());
        model.addAttribute("totalItems", events.getTotalElements());
        model.addAttribute("pageSize", size);
        model.addAttribute("text", text);
        model.addAttribute("begin", begin);
        model.addAttribute("end", end);
        model.addAttribute("showCatalogId", showCatalogId);

        return "event/events";
    }

    @GetMapping("/projects/catalogs/events/build")
    public String getEventBuild(){
        return "event/build";
    }

    @GetMapping("/projects/catalogs/{projectSlug}")
    public String getCatalogWithProjectSlug(@PathVariable("projectSlug") String projectSlug,
                             Model model){
        model.addAttribute("catalogs", catalogService.findAllCatalogsByProjectSlug(projectSlug));
        return "catalog/catalogsWithProjectSlug";
    }

    @GetMapping("/projects/catalogs/events/{catalogSlug}")
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
