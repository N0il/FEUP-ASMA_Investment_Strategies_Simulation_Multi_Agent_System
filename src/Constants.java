import java.util.Arrays;
import java.util.List;

public final class Constants {
    public static final long MARKET_DAILY_PERIOD = 5000; // 5 seconds
    public static final String MARKET_AGENT_NAME = "market-agent";
    public static final List<String> BROKER_AGENT_NAMES = Arrays.asList("broker-agent", "broker-agent-2");
    public static final String EXCHANGE_AGENT_NAME = "exchange-agent";
    public static final String MARKET_NO_MORE_DAYS_MSG = "NO_MORE_DAYS";
    public static final String UNSUPPORTED_ORDER_TYPE = "UNSUPPORTED_ORDER";
    public static final String DATA_FILENAME = "../data.json";
    public static enum ORDER_TYPES {
        BUY("BUY"),
        SELL("SELL"),
        SHORT("SHORT");

        ORDER_TYPES(String buy) {
        }
    }

    private Constants() {
        // private constructor to prevent instantiation
    }
}
