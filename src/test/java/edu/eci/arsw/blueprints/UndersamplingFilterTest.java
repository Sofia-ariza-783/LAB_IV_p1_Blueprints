package edu.eci.arsw.blueprints;

import edu.eci.arsw.blueprints.filters.UndersamplingFilter;
import edu.eci.arsw.blueprints.model.Blueprint;
import edu.eci.arsw.blueprints.model.Point;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class UndersamplingFilterTest {

    private final UndersamplingFilter filter = new UndersamplingFilter();

    @Test
    void apply_keepsEvenIndexedPoints() {
        Blueprint bp = new Blueprint("author", "bp",
                List.of(new Point(0, 0), new Point(1, 1), new Point(2, 2),
                        new Point(3, 3), new Point(4, 4)));

        Blueprint result = filter.apply(bp);

        assertEquals(List.of(new Point(0, 0), new Point(2, 2), new Point(4, 4)),
                result.getPoints());
    }

    @Test
    void apply_twoOrFewerPointsReturnedUnchanged() {
        Blueprint bp = new Blueprint("author", "bp",
                List.of(new Point(1, 1), new Point(2, 2)));

        Blueprint result = filter.apply(bp);

        assertEquals(2, result.getPoints().size());
    }

    @Test
    void apply_emptyListReturnsOriginal() {
        Blueprint bp = new Blueprint("author", "bp", List.of());

        Blueprint result = filter.apply(bp);

        assertTrue(result.getPoints().isEmpty());
    }

    @Test
    void apply_singlePointReturnedUnchanged() {
        Blueprint bp = new Blueprint("author", "bp", List.of(new Point(7, 7)));

        Blueprint result = filter.apply(bp);

        assertEquals(1, result.getPoints().size());
    }

    @Test
    void apply_fourPointsReturnTwo() {
        Blueprint bp = new Blueprint("author", "bp",
                List.of(new Point(0, 0), new Point(1, 1),
                        new Point(2, 2), new Point(3, 3)));

        Blueprint result = filter.apply(bp);

        assertEquals(2, result.getPoints().size());
        assertEquals(new Point(0, 0), result.getPoints().get(0));
        assertEquals(new Point(2, 2), result.getPoints().get(1));
    }
}
