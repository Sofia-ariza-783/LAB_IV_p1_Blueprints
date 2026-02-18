package edu.eci.arsw.blueprints;

import edu.eci.arsw.blueprints.filters.BlueprintsFilter;
import edu.eci.arsw.blueprints.model.Blueprint;
import edu.eci.arsw.blueprints.model.Point;
import edu.eci.arsw.blueprints.persistence.BlueprintNotFoundException;
import edu.eci.arsw.blueprints.persistence.BlueprintPersistence;
import edu.eci.arsw.blueprints.persistence.BlueprintPersistenceException;
import edu.eci.arsw.blueprints.services.BlueprintsServices;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class BlueprintsServicesTest {

    @Mock
    private BlueprintPersistence persistence;

    @Mock
    private BlueprintsFilter filter;

    @InjectMocks
    private BlueprintsServices services;

    private Blueprint sampleBp;

    @BeforeEach
    void setUp() {
        sampleBp = new Blueprint("john", "house",
                List.of(new Point(0, 0), new Point(10, 10)));
    }

    @Test
    void addNewBlueprint_delegatesToPersistence() throws BlueprintPersistenceException {
        services.addNewBlueprint(sampleBp);
        verify(persistence, times(1)).saveBlueprint(sampleBp);
    }

    @Test
    void getAllBlueprints_returnsAllFromPersistence() {
        Set<Blueprint> expected = Set.of(sampleBp);
        when(persistence.getAllBlueprints()).thenReturn(expected);

        Set<Blueprint> result = services.getAllBlueprints();

        assertEquals(expected, result);
    }

    @Test
    void getBlueprintsByAuthor_returnsCorrectSet() throws BlueprintNotFoundException {
        Set<Blueprint> expected = Set.of(sampleBp);
        when(persistence.getBlueprintsByAuthor("john")).thenReturn(expected);

        Set<Blueprint> result = services.getBlueprintsByAuthor("john");

        assertEquals(expected, result);
    }

    @Test
    void getBlueprint_appliesFilterBeforeReturning() throws BlueprintNotFoundException {
        Blueprint filtered = new Blueprint("john", "house", List.of(new Point(0, 0)));
        when(persistence.getBlueprint("john", "house")).thenReturn(sampleBp);
        when(filter.apply(sampleBp)).thenReturn(filtered);

        Blueprint result = services.getBlueprint("john", "house");

        assertEquals(filtered, result);
        verify(filter).apply(sampleBp);
    }

    @Test
    void getBlueprint_throwsWhenNotFound() throws BlueprintNotFoundException {
        when(persistence.getBlueprint("nobody", "ghost"))
                .thenThrow(new BlueprintNotFoundException("Blueprint not found: nobody/ghost"));

        assertThrows(BlueprintNotFoundException.class,
                () -> services.getBlueprint("nobody", "ghost"));
    }
}
