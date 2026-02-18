package edu.eci.arsw.blueprints;

import edu.eci.arsw.blueprints.filters.RedundancyFilter;
import edu.eci.arsw.blueprints.model.Blueprint;
import edu.eci.arsw.blueprints.model.Point;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class RedundancyFilterTest {

    private final RedundancyFilter filter = new RedundancyFilter();

    @Test
    void apply_removesConsecutiveDuplicates() {
        Blueprint bp = new Blueprint("author", "bp",
                List.of(new Point(1, 1), new Point(1, 1), new Point(2, 2)));

        Blueprint result = filter.apply(bp);

        assertEquals(2, result.getPoints().size());
        assertEquals(new Point(1, 1), result.getPoints().get(0));
        assertEquals(new Point(2, 2), result.getPoints().get(1));
    }

    @Test
    void apply_keepsNonConsecutiveDuplicates() {
        Blueprint bp = new Blueprint("author", "bp",
                List.of(new Point(1, 1), new Point(2, 2), new Point(1, 1)));

        Blueprint result = filter.apply(bp);

        assertEquals(3, result.getPoints().size());
    }

    @Test
    void apply_emptyListReturnsOriginal() {
        Blueprint bp = new Blueprint("author", "bp", List.of());

        Blueprint result = filter.apply(bp);

        assertTrue(result.getPoints().isEmpty());
    }

    @Test
    void apply_allDuplicatesCollapsesToOne() {
        Blueprint bp = new Blueprint("author", "bp",
                List.of(new Point(5, 5), new Point(5, 5), new Point(5, 5)));

        Blueprint result = filter.apply(bp);

        assertEquals(1, result.getPoints().size());
    }

    @Test
    void apply_noDuplicatesReturnsSamePoints() {
        List<Point> pts = List.of(new Point(1, 2), new Point(3, 4), new Point(5, 6));
        Blueprint bp = new Blueprint("author", "bp", pts);

        Blueprint result = filter.apply(bp);

        assertEquals(pts, result.getPoints());
    }
}
