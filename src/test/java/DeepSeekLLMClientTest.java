import com.promising.jarvis.llm.client.impl.DeepSeekLLMClient;
import org.junit.Before;

public class DeepSeekLLMClientTest {
    private DeepSeekLLMClient deepSeekLlmClient;

    @Before
    public void setUp() {
        // 初始化 DeepSeekLLMClient
        deepSeekLlmClient = new DeepSeekLLMClient();
    }

}