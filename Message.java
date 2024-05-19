import com.ericsson.otp.erlang.OtpErlangLong;
import com.ericsson.otp.erlang.OtpErlangPid;
import com.ericsson.otp.erlang.OtpErlangTuple;

public class Message {
    private OtpErlangPid pid;
    private String senderAddress;
    private String content;
    private String timeStamp;
    private String type;

    public Message(OtpErlangTuple msgTuple) {
        this.pid = (OtpErlangPid) msgTuple.elementAt(0);

        this.senderAddress = msgTuple.elementAt(1).toString();
        this.content = msgTuple.elementAt(2).toString();
        this.timeStamp = msgTuple.elementAt(3).toString();
        this.type = msgTuple.elementAt(4).toString();
    }

    public OtpErlangPid getPid() {
        return pid;
    }

    public String getSenderAddress() {
        return senderAddress;
    }

    public String getContent() {
        return content;
    }

    public String getTimeStamp() {
        return timeStamp;
    }

    public String getType() {
        return type;
    }

    public String getSenderName() {
        if (senderAddress != null && senderAddress.contains("@")) {
            return senderAddress.split("@")[0];
        }
        return " ";
    }
}
