package message;

import lombok.AllArgsConstructor;
import lombok.Data;


@Data
@AllArgsConstructor
public class MessageWrapper {
    private String  answerFlag;
    private String toIP;
    private int toPort;
    private String toName;
    private String fromIP;
    private int fromPort;
    private String fromName;
    private int ttl;
    private long timestamp;
    private String message;
}
