package edu.eci.arsw.blueprints.controllers;

import edu.eci.arsw.blueprints.model.Blueprint;
import edu.eci.arsw.blueprints.model.Point;
import edu.eci.arsw.blueprints.persistence.BlueprintNotFoundException;
import edu.eci.arsw.blueprints.persistence.BlueprintPersistenceException;
import edu.eci.arsw.blueprints.services.BlueprintsServices;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.validation.FieldError;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/v1/blueprints")
public class BlueprintsAPIController {

    private final BlueprintsServices services;

    public BlueprintsAPIController(BlueprintsServices services) {
        this.services = services;
    }

    @Operation(summary = "Get all blueprints", description = "Returns the complete list of blueprints stored in the system.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Blueprints retrieved successfully")
    })
    @GetMapping
    public ResponseEntity<ApiResponseFormated<Set<Blueprint>>> getAll() {
        Set<Blueprint> blueprints = services.getAllBlueprints();
        return ResponseEntity.ok(new ApiResponseFormated<>(200, "execute ok", blueprints));
    }

    @Operation(summary = "Get blueprints by author", description = "Returns all blueprints belonging to the specified author.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Blueprints retrieved successfully"),
            @ApiResponse(responseCode = "404", description = "No blueprints found for the given author")
    })
    @GetMapping("/{author}")
    public ResponseEntity<ApiResponseFormated<?>> byAuthor(@PathVariable String author) {
        try {
            Set<Blueprint> blueprints = services.getBlueprintsByAuthor(author);
            return ResponseEntity.ok(new ApiResponseFormated<>(200, "execute ok", blueprints));
        } catch (BlueprintNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(
                    new ApiResponseFormated<>(404, e.getMessage(), null)
            );
        }
    }

    @Operation(summary = "Get a blueprint by author and name", description = "Returns a single blueprint identified by its author and name. The configured filter is applied before returning.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Blueprint retrieved successfully"),
            @ApiResponse(responseCode = "404", description = "Blueprint not found")
    })
    @GetMapping("/{author}/{bpname}")
    public ResponseEntity<ApiResponseFormated<?>> byAuthorAndName(
            @PathVariable String author,
            @PathVariable String bpname) {
        try {
            Blueprint bp = services.getBlueprint(author, bpname);
            return ResponseEntity.ok(new ApiResponseFormated<>(200, "execute ok", bp));
        } catch (BlueprintNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(
                    new ApiResponseFormated<>(404, e.getMessage(), null)
            );
        }
    }


    @Operation(summary = "Create a new blueprint", description = "Persists a new blueprint with the provided author, name, and list of points.")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Blueprint created successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid request body"),
            @ApiResponse(responseCode = "403", description = "Blueprint already exists")
    })
    @PostMapping
    public ResponseEntity<ApiResponseFormated<?>> add(@Valid @RequestBody NewBlueprintRequest req) {
        try {
            Blueprint bp = new Blueprint(req.author(), req.name(), req.points());
            services.addNewBlueprint(bp);
            return ResponseEntity.status(HttpStatus.CREATED).body(new ApiResponseFormated<>(201, "Blueprint created successfully", null));
        } catch (BlueprintPersistenceException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(new ApiResponseFormated<>(403, e.getMessage(), null));
        }
    }

    @Operation(summary = "Add a point to a blueprint", description = "Appends a new point (x, y) to an existing blueprint identified by author and name.")
    @ApiResponses({
            @ApiResponse(responseCode = "202", description = "Point added successfully"),
            @ApiResponse(responseCode = "404", description = "Blueprint not found")
    })
    @PutMapping("/{author}/{bpname}/points")
    public ResponseEntity<ApiResponseFormated<?>> addPoint(@PathVariable String author, @PathVariable String bpname, @RequestBody Point p) {
        try {
            services.addPoint(author, bpname, p.x(), p.y());
            return ResponseEntity.status(HttpStatus.ACCEPTED).body(new ApiResponseFormated<>(202, "Point added successfully", null));
        } catch (BlueprintNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body( new ApiResponseFormated<>(404, e.getMessage(), null));
        }
    }

    @Operation(summary = "Handle validation errors", description = "Catches @Valid constraint violations and returns a structured 400 response.")
    @ApiResponse(responseCode = "400", description = "One or more fields failed validation")
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiResponseFormated<?>> handleValidationErrors(
            MethodArgumentNotValidException ex) {
        String message = ex.getBindingResult()
                .getFieldErrors()
                .stream()
                .map(FieldError::getDefaultMessage)
                .collect(Collectors.joining(", "));
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new ApiResponseFormated<>(400, "Validation error: " + message, null));
    }

    public record ApiResponseFormated<T>(int code, String message, T data) {}

    public record NewBlueprintRequest(
            @NotBlank String author,
            @NotBlank String name,
            @Valid List<Point> points
    ) {}
}