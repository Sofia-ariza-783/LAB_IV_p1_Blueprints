package edu.eci.arsw.blueprints;

import edu.eci.arsw.blueprints.controllers.BlueprintsAPIController;
import edu.eci.arsw.blueprints.model.Blueprint;
import edu.eci.arsw.blueprints.model.Point;
import edu.eci.arsw.blueprints.persistence.BlueprintNotFoundException;
import edu.eci.arsw.blueprints.persistence.BlueprintPersistenceException;
import edu.eci.arsw.blueprints.services.BlueprintsServices;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;
import java.util.Set;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(BlueprintsAPIController.class)
class BlueprintsAPIControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private BlueprintsServices services;

    @Test
    void getAll_returns200WithBlueprints() throws Exception {
        Blueprint bp = new Blueprint("john", "house", List.of(new Point(0, 0)));
        when(services.getAllBlueprints()).thenReturn(Set.of(bp));

        mockMvc.perform(get("/api/v1/blueprints"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));
    }

    @Test
    void byAuthor_returns200WhenFound() throws Exception {
        Blueprint bp = new Blueprint("john", "house", List.of(new Point(0, 0)));
        when(services.getBlueprintsByAuthor("john")).thenReturn(Set.of(bp));

        mockMvc.perform(get("/api/v1/blueprints/john"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));
    }

    @Test
    void byAuthor_returns404WhenNotFound() throws Exception {
        when(services.getBlueprintsByAuthor("ghost"))
                .thenThrow(new BlueprintNotFoundException("No blueprints for author: ghost"));

        mockMvc.perform(get("/api/v1/blueprints/ghost"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value(404));
    }

    @Test
    void add_returns201WhenCreated() throws Exception {
        mockMvc.perform(post("/api/v1/blueprints")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "author": "alice",
                                  "name": "lab",
                                  "points": [{"x":1,"y":2}]
                                }
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.code").value(201));
    }

    @Test
    void add_returns403WhenDuplicate() throws Exception {
        doThrow(new BlueprintPersistenceException("Blueprint already exists"))
                .when(services).addNewBlueprint(any());

        mockMvc.perform(post("/api/v1/blueprints")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "author": "alice",
                                  "name": "lab",
                                  "points": [{"x":1,"y":2}]
                                }
                                """))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.code").value(403));
    }
}
