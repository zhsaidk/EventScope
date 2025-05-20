package com.zhsaidk.http.controller;

import com.zhsaidk.database.entity.Catalog;
import com.zhsaidk.database.entity.Event;
import com.zhsaidk.database.entity.PermissionRole;
import com.zhsaidk.database.entity.Project;
import com.zhsaidk.database.repo.CatalogRepository;
import com.zhsaidk.database.repo.UserRepository;
import com.zhsaidk.dto.BuildCreateCatalogDto;
import com.zhsaidk.dto.BuildEventWebDto;
import com.zhsaidk.dto.BuildProjectDTO;
import com.zhsaidk.dto.BuildReadCatalogDto;
import com.zhsaidk.service.*;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Controller
@RequiredArgsConstructor
public class ProjectController {

    private final CatalogService catalogService;
    private final EventService eventService;
    private final ProjectService projectService;
    private final UserRepository userRepository;
    private final PermissionService permissionService;
    private final UserService userService;

    // === Projects ===

    @GetMapping("/projects")
    public String getProjects(Model model,
                              @RequestParam(value = "page", defaultValue = "0") Integer page,
                              @RequestParam(value = "size", defaultValue = "10") Integer size,
                              Authentication authentication) {
        PageRequest pageRequest = PageRequest.of(page, size, Sort.by("createdAt"));
        Page<Project> projectPage = projectService.getAllProjects(pageRequest, authentication);

        model.addAttribute("projects", projectPage.getContent());
        model.addAttribute("currentPage", projectPage.getNumber());
        model.addAttribute("totalPages", projectPage.getTotalPages());
        model.addAttribute("totalItems", projectPage.getTotalElements());
        model.addAttribute("pageSize", size);

        return "project/projects";
    }

    @GetMapping("/projects/build")
    public String getProjectCreate(Model model) {
        model.addAttribute("project", new BuildProjectDTO());
        return "project/build";
    }

    @PostMapping("/projects")
    public String createProject(@Valid @ModelAttribute("project") BuildProjectDTO dto,
                                Authentication authentication,
                                BindingResult bindingResult,
                                Model model) {
        if (bindingResult.hasErrors()) {
            model.addAttribute("project", dto);
            return "project/build";
        }
        projectService.build(dto, authentication);
        return "redirect:/projects";
    }

    @GetMapping("/projects/{projectSlug}")
    public String getProjectPage(Model model,
                                 @PathVariable("projectSlug") String projectSlug,
                                 Authentication authentication) {
        model.addAttribute("project", projectService.getProjectByProjectSlug(projectSlug, authentication));
        model.addAttribute("users", userService.findAllUsersAsDto());
        return "project/project";
    }

    @PostMapping("/projects/{projectSlug}")
    public String modifyProject(@PathVariable("projectSlug") String projectSlug,
                                @Valid BuildProjectDTO dto,
                                BindingResult bindingResult,
                                RedirectAttributes redirectAttributes,
                                Authentication authentication) {
        if (bindingResult.hasErrors()) {
            redirectAttributes.addFlashAttribute("errors", bindingResult.getAllErrors());
            return "redirect:/projects/" + projectSlug;
        }
        projectService.modify(projectSlug, dto, authentication);
        return "redirect:/projects";
    }

    @PostMapping("/projects/share")
    public String share(@RequestParam("projectSlug") String projectSlug,
                        @RequestParam("toUserId") Integer toUserId,
                        @RequestParam("role") PermissionRole role,
                        Authentication authentication) {

        permissionService.grantPermission(projectSlug, toUserId, role, authentication);

        return "redirect:/projects";
    }

    @PostMapping("/projects/delete/{slug}")
    public String deleteProject(@PathVariable String slug,
                                Authentication authentication) {
        projectService.remove(slug, authentication);
        return "redirect:/projects";
    }

    // === Catalogs ===

    @GetMapping("/projects/catalogs")
    public String getCatalogs(Model model,
                              @RequestParam(value = "page", defaultValue = "0") Integer page,
                              @RequestParam(value = "size", defaultValue = "10") Integer size,
                              Authentication authentication) {
        Page<Catalog> catalogs = catalogService.findAllCatalogs(PageRequest.of(page, size, Sort.by("createdAt").ascending()), authentication);
        model.addAttribute("catalogs", catalogs.getContent());
        model.addAttribute("currentPage", catalogs.getNumber());
        model.addAttribute("totalPages", catalogs.getTotalPages());
        model.addAttribute("totalItems", catalogs.getTotalElements());
        model.addAttribute("pageSize", size);
        return "catalog/catalogs";
    }

    @GetMapping("/projects/catalogs/build")
    public String getCatalogsBuild() {
        return "catalog/build";
    }

    @PostMapping("/projects/{catalogSlug}/catalogs")
    public String createCatalog(@Valid BuildCreateCatalogDto dto,
                                BindingResult bindingResult,
                                RedirectAttributes redirectAttributes,
                                @PathVariable("catalogSlug") String catalogSlug,
                                Authentication authentication) {
        if (bindingResult.hasErrors()) {
            redirectAttributes.addFlashAttribute("errors", bindingResult.getAllErrors());
            return "redirect:/projects/catalogs/build";
        }
        catalogService.build(dto, catalogSlug, authentication);
        return "redirect:/projects/catalogs";
    }

    @PostMapping("/projects/{projectSlug}/catalogs/{catalogSlug}/delete")
    public String deleteCatalog(@PathVariable("projectSlug") String projectSlug,
                                @PathVariable("catalogSlug") String catalogSlug,
                                Authentication authentication) {
        catalogService.remove(projectSlug, catalogSlug, authentication);
        return "redirect:/projects/catalogs";
    }

    @GetMapping("/projects/catalogs/{catalogSlug}/edit")
    public String getCatalogPage(@PathVariable("catalogSlug") String catalogSlug,
                                 Model model){
        model.addAttribute("catalog", catalogService.findCatalogByCatalogSlug(catalogSlug));
        return "catalog/catalog";
    }

    @PostMapping("/projects/{projectSlug}/catalogs/{catalogSlug}")
    public String updateCatalog(
            @PathVariable("projectSlug") String projectSlug,
            @PathVariable("catalogSlug") String catalogSlug,
            BuildReadCatalogDto dto,
            BindingResult bindingResult,
            Authentication authentication) {
        if (bindingResult.hasErrors()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST);
        }

        catalogService.update(projectSlug, catalogSlug, dto, authentication);
        return "redirect:/projects/catalogs/" + catalogSlug + "/edit";
    }

    @GetMapping("/projects/catalogs/{projectSlug}")
    public String getCatalogWithProjectSlug(@PathVariable("projectSlug") String projectSlug,
                                            Authentication authentication,
                                            Model model) {

        if (!permissionService.hasAnyPermission(projectSlug, authentication)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "you have not any permission");
        }

        model.addAttribute("catalogs", catalogService.findAllCatalogsByProjectSlug(projectSlug, authentication));
        return "catalog/catalogsWithProjectSlug";
    }

    // === Events ===

    @GetMapping("/projects/catalogs/events")
    public String getEvents(Model model,
                            @RequestParam(value = "page", defaultValue = "0") Integer page,
                            @RequestParam(value = "size", defaultValue = "10") Integer size,
                            @RequestParam(value = "text", required = false) String text,
                            @RequestParam(value = "begin", required = false) LocalDateTime begin,
                            @RequestParam(value = "end", required = false) LocalDateTime end,
                            @RequestParam(value = "showCatalogId", required = false) Long showCatalogId,
                            Authentication authentication) {
        Page<Event> events = eventService.findAllEvents(PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt")), text, begin, end, authentication);

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
    public String getEventBuild() {
        return "event/build";
    }

    @PostMapping("/projects/{projectSlug}/catalogs/{catalogSlug}/events")
    public String createEvent(@Valid BuildEventWebDto dto,
                              @PathVariable("projectSlug") String projectSlug,
                              @PathVariable("catalogSlug") String catalogSlug,
                              BindingResult bindingResult,
                              RedirectAttributes redirectAttributes,
                              Authentication authentication) {
        if (bindingResult.hasErrors()) {
            redirectAttributes.addFlashAttribute("errors", bindingResult.getAllErrors());
            return "redirect:/projects/catalogs/events/build";
        }
        eventService.createEvent(dto, projectSlug, catalogSlug, authentication);
        return "redirect:/projects/catalogs/events";
    }

    @PostMapping("/projects/{projectSlug}/catalogs/{catalogSlug}/events/delete")
    public String deleteEvent(@RequestParam("EventId") UUID eventId,
                              @PathVariable("projectSlug") String projectSlug,
                              @PathVariable("catalogSlug") String catalogSlug,
                              Authentication authentication,
                              RedirectAttributes redirectAttributes) {
        try {
            eventService.remove(projectSlug, catalogSlug, eventId, authentication);
            redirectAttributes.addFlashAttribute("message", "Событие успешно удалено");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Ошибка при удалении события: " + e.getMessage());
        }
        return "redirect:/projects/catalogs/events";
    }

    @GetMapping("/projects/catalogs/events/{catalogSlug}")
    public String getEventsWithCatalogSlug(@PathVariable("catalogSlug") String catalogSlug,
                                           Model model,
                                           Authentication authentication,
                                           @RequestParam(name = "page", defaultValue = "0") Integer page,
                                           @RequestParam(name = "size", defaultValue = "10") Integer size) {


        model.addAttribute("events", eventService.findAllEventsByCatalogSlug(catalogSlug, authentication, page, size));
        return "event/eventsWithCatalogSlug";
    }

    // === Auth ===

    @GetMapping("/login")
    public String loginPage() {
        return "user/login";
    }
}
