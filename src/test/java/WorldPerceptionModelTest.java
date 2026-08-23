import com.promising.jarvis.core.perception.GamePhase;
import com.promising.jarvis.core.perception.GamePhaseObservation;
import org.junit.Test;

import java.util.List;

import static org.junit.Assert.*;

public class WorldPerceptionModelTest {
    @Test public void phaseObservationIsBoundedAndImmutable() {
        var observation = new GamePhaseObservation(GamePhase.NETHER, 1.4, List.of("维度"));
        assertEquals(GamePhase.NETHER, observation.phase());
        assertEquals(1.0, observation.confidence(), 0.001);
        assertThrows(UnsupportedOperationException.class, () -> observation.evidence().add("x"));
    }
}
