import com.promising.jarvis.core.world.LocationComponent;
import org.junit.Test;

import static org.junit.Assert.assertTrue;

public class LocationContextComponentTest {
    private final LocationComponent component = new LocationComponent();

    @Test
    public void recognizesChineseLocationQuestions() {
        assertTrue(component.supports("我现在在哪儿"));
        assertTrue(component.supports("我在哪里"));
        assertTrue(component.supports("查看当前位置和坐标"));
    }

    @Test
    public void recognizesEnglishLocationQuestions() {
        assertTrue(component.supports("where am I"));
        assertTrue(component.supports("show my coordinates"));
    }
}
