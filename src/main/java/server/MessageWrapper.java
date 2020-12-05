package server;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.sql.Timestamp;
@Data
@AllArgsConstructor
public class MessageWrapper {
    int answerFlag;
    String toIP;
    int toPort;
    String toName;
    String fromIP;
    int fromPort;
    String fromName;
    int ttl;
    Timestamp timestamp;
    String message;
}
