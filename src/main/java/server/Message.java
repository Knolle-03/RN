package server;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.sql.Timestamp;
@Data
@NoArgsConstructor
public class Message {
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
