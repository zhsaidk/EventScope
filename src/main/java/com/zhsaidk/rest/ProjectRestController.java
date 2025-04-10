package com.zhsaidk.rest;

import com.zhsaidk.database.entity.Catalog;
import com.zhsaidk.database.entity.Event;
import com.zhsaidk.database.entity.Project;
import com.zhsaidk.dto.BuildCatalogDto;
import com.zhsaidk.dto.BuildEventDto;
import com.zhsaidk.dto.BuildProjectDTO;
import com.zhsaidk.dto.SearchEventsDto;
import com.zhsaidk.service.CatalogService;
import com.zhsaidk.service.EventService;
import com.zhsaidk.service.ProjectService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/projects")
@RequiredArgsConstructor
public class ProjectRestController {
    private final ProjectService projectService;
    private final CatalogService catalogService;
    private final EventService eventService;

    @GetMapping
    public ResponseEntity<List<Project>> getAllProject(){
        return ResponseEntity.ok(projectService.getAll());
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getProjectById(@PathVariable("id") Integer id){
        return projectService.getById(id);
    }

    @PostMapping("/build")
    public ResponseEntity<?> buildProject(@Valid @RequestBody BuildProjectDTO dto,
                                         BindingResult bindingResult){
        if (bindingResult.hasErrors()){
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(bindingResult.getAllErrors());
        }
        return projectService.build(dto);
    }

    @PutMapping("/update/{id}")
    public ResponseEntity<?> modifyProject(@PathVariable("id") Integer id,
                                    @Valid @RequestBody BuildProjectDTO dto,
                                    BindingResult bindingResult){
        if (bindingResult.hasErrors()){
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(bindingResult.getAllErrors());
        }
        return projectService.modify(id, dto);
    }

    @DeleteMapping("/delete/{id}")
    public ResponseEntity<?> removeProject(@PathVariable("id") Integer id){
        return projectService.remove(id)
                ? ResponseEntity.noContent().build()
                : ResponseEntity.notFound().build();
    }

    // TODO ----------------------Для Категории ------------------------------


    @GetMapping("/catalogs")
    public ResponseEntity<List<Catalog>> getAllCatalogs(){
        return catalogService.getAll();
    }

    @GetMapping("/catalogs/{id}")
    public ResponseEntity<?> getCatalogById(@PathVariable("id") Integer id){
        return catalogService.findById(id);
    }

    @PostMapping("/catalogs/build")
    public ResponseEntity<?> buildCatalog(@Valid @RequestBody BuildCatalogDto dto,
                                           BindingResult bindingResult){
        if (bindingResult.hasErrors()){
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(bindingResult.getAllErrors());
        }

        return catalogService.build(dto);
    }

    @PutMapping("/catalogs/{id}")
    public ResponseEntity<?> update(@PathVariable("id") Integer id,
                                    @Valid @RequestBody BuildCatalogDto dto,
                                    BindingResult bindingResult){
        if (bindingResult.hasErrors()){
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(bindingResult.getAllErrors());
        }
        return catalogService.update(id, dto);
    }

    @DeleteMapping("/catalogs/{id}")
    public ResponseEntity<?> removeCatalog(@PathVariable("id") Integer id){
        return catalogService.remove(id)
                ? ResponseEntity.noContent().build()
                : ResponseEntity.notFound().build();
    }


    //TODO ---------------------Для Events--------------------------

    @PostMapping("/catalogs/events/build")
    public ResponseEntity<?> builtEvent(@Valid @RequestBody BuildEventDto dto,
                                        BindingResult bindingResult){
        if (bindingResult.hasErrors()){
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(bindingResult.getAllErrors());
        }
        return eventService.build(dto);
    }

    @DeleteMapping("/catalogs/events/{id}")
    public ResponseEntity<?> removeEvent(@PathVariable("id") UUID id){
        return eventService.remove(id)
                ? ResponseEntity.noContent().build()
                : ResponseEntity.notFound().build();
    }

    @GetMapping("/catalogs/events/{id}")
    public ResponseEntity<?> getEventById(@PathVariable("id") UUID id){
        return eventService.getById(id);
    }

    @PutMapping("/catalogs/events/{id}")
    public ResponseEntity<?> updateEvent(@PathVariable("id") UUID id,
                                         @Valid @RequestBody BuildEventDto dto,
                                         BindingResult bindingResult){
        if (bindingResult.hasErrors()){
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(bindingResult);
        }
        return eventService.update(id, dto);
    }

    @PostMapping("/catalogs/events/search")
    public ResponseEntity<?> getByParameters(@Valid @RequestBody SearchEventsDto dto,
                                                       BindingResult bindingResult) {

        if (bindingResult.hasErrors()){
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(bindingResult.getAllErrors());
        }
        return eventService.findByParameters(dto);
    }
}
