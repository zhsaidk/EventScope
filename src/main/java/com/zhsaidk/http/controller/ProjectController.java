package com.zhsaidk.http.controller;

import com.zhsaidk.database.entity.Catalog;
import com.zhsaidk.database.entity.Event;
import com.zhsaidk.database.entity.Project;
import com.zhsaidk.database.repo.EventSpecification;
import com.zhsaidk.dto.BuildCreateCatalogDto;
import com.zhsaidk.dto.BuildEventDto;
import com.zhsaidk.dto.BuildEventWebDto;
import com.zhsaidk.dto.BuildProjectDTO;
import com.zhsaidk.service.CatalogService;
import com.zhsaidk.service.EventService;
import com.zhsaidk.service.ProjectService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.LocalDateTime;
import java.util.UUID;

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

    @GetMapping("/projects/{projectSlug}")
    public String geProjectPage(Model model,
                                @PathVariable("projectSlug") String projectSlug) {
        model.addAttribute("project", projectService.getProjectBySlug(projectSlug));
        return "project/project";
    }

    @PostMapping("/projects/{projectSlug}")
    public String modifyProject(@PathVariable("projectSlug") String projectSlug,
                                @Valid BuildProjectDTO dto,
                                BindingResult bindingResult,
                                RedirectAttributes redirectAttributes) {
        if (bindingResult.hasErrors()) {
            redirectAttributes.addFlashAttribute("errors", bindingResult.getAllErrors());
            return "redirect:/projects/" + projectSlug;
        }
        projectService.modify(projectSlug, dto);
        return "redirect:/projects";
    }


    @GetMapping("/projects/build")
    public String getProjectCreate(Model model) {
        model.addAttribute("project", new BuildProjectDTO());
        return "project/build"; // Maps to project/build.html
    }

    @PostMapping("/projects")
    public String createProject(@Valid @ModelAttribute("project") BuildProjectDTO dto,
                                BindingResult bindingResult,
                                Model model) {
        if (bindingResult.hasErrors()) {
            model.addAttribute("project", dto); // Preserve form input
            return "project/build"; // Render form with errors
        }
        projectService.build(dto);
        return "redirect:/projects";
    }

    @GetMapping("/projects/catalogs")
    public String getCatalogs(Model model,
                              @RequestParam(value = "page", defaultValue = "0") Integer page,
                              @RequestParam(value = "size", defaultValue = "10") Integer size) {
        Page<Catalog> catalogs = catalogService.findAllCatalogs(PageRequest.of(page, size, Sort.by("createdAt").ascending()));
        model.addAttribute("catalogs", catalogs.getContent());
        model.addAttribute("currentPage", catalogs.getNumber());
        model.addAttribute("totalPages", catalogs.getTotalPages());
        model.addAttribute("totalItems", catalogs.getTotalElements());
        model.addAttribute("pageSize", size);
        return "catalog/catalogs";
    }

    @PostMapping("/projects/catalogs/{catalogSlug}")
    public String deleteCatalog(@PathVariable("catalogSlug") String catalogSlug){
        catalogService.remove(catalogSlug);
        return "redirect:/projects/catalogs";
    }
//
//    @GetMapping("/projects/catalogs/{catalogSlug}")
//    public String getCatalog(Model model,
//                                @PathVariable("catalogSlug") String catalogSlug) {
//        model.addAttribute("project", projectService.getProjectBySlug(catalogSlug));
//        return "catalog/catalog";
//    }

    @GetMapping("/projects/catalogs/build")
    public String getCatalogsBuild() {
        return "catalog/build";
    }

    @PostMapping("/projects/{catalogSlug}/catalogs")
    public String createCatalog(@Valid BuildCreateCatalogDto dto,
                                BindingResult bindingResult,
                                RedirectAttributes redirectAttributes,
                                @PathVariable("catalogSlug") String catalogSlug) {
        if (bindingResult.hasErrors()) {
            redirectAttributes.addFlashAttribute("errors", bindingResult.getAllErrors());
            return "redirect:/projects/catalogs/build";
        }
        catalogService.build(dto, catalogSlug);
        return "redirect:/projects/catalogs";
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

    @PostMapping("/projects/catalogs/events")
    public String deleteEvent(@RequestParam("EventId") UUID eventId, RedirectAttributes redirectAttributes) {
        try {
            eventService.remove(eventId);
            redirectAttributes.addFlashAttribute("message", "Событие успешно удалено");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Ошибка при удалении события: " + e.getMessage());
        }
        return "redirect:/projects/catalogs/events";
    }

    @GetMapping("/projects/catalogs/events/build")
    public String getEventBuild() {
        return "event/build";
    }

    @PostMapping("/projects/{projectSlug}/catalogs/{catalogSlug}/events")
    public String createEvent(@Valid BuildEventWebDto dto,
                              @PathVariable("projectSlug") String projectSlug,
                              @PathVariable("catalogSlug") String catalogSlug,
                              BindingResult bindingResult,
                              RedirectAttributes redirectAttributes) {
        if (bindingResult.hasErrors()) {
            redirectAttributes.addFlashAttribute("errors", bindingResult.getAllErrors());
            return "redirect:/projects/catalogs/events/build";
        }
        eventService.createEvent(dto, projectSlug, catalogSlug);
        return "redirect:/projects/catalogs/events";
    }

    @GetMapping("/projects/catalogs/{projectSlug}")
    public String getCatalogWithProjectSlug(@PathVariable("projectSlug") String projectSlug,
                                            Model model) {
        model.addAttribute("catalogs", catalogService.findAllCatalogsByProjectSlug(projectSlug));
        return "catalog/catalogsWithProjectSlug";
    }

    @GetMapping("/projects/catalogs/events/{catalogSlug}")
    public String getEventsWithCatalogSlug(@PathVariable("catalogSlug") String catalogSlug,
                                           Model model) {
        model.addAttribute("events", eventService.findAllEventsByCatalogSlug(catalogSlug));
        return "event/eventsWithCatalogSlug";
    }

    @GetMapping("/login")
    public String loginPage() {
        return "user/login";
    }
}
