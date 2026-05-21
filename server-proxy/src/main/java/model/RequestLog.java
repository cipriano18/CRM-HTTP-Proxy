/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package model;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 *
 * @author cipriano
 */
public class RequestLog {
private static final DateTimeFormatter FMT =
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
 
    private final String clientIp;
    private final String url;
    private final String method;
    private final String status;   // PERMITIDO o BLOQUEADO
    private final long   bytes;
    private final String timestamp;
 
    public RequestLog(String clientIp, String url, String method,
                      String status, long bytes) {
        this.clientIp  = clientIp;
        this.url       = url;
        this.method    = method;
        this.status    = status;
        this.bytes     = bytes;
        this.timestamp = LocalDateTime.now().format(FMT);
    }
 
    public String getClientIp()  { return clientIp;  }
    public String getUrl()       { return url;        }
    public String getMethod()    { return method;     }
    public String getStatus()    { return status;     }
    public long   getBytes()     { return bytes;      }
    public String getTimestamp() { return timestamp;  }
    public boolean isBlocked()   { return "BLOQUEADO".equals(status); }
 
    @Override
    public String toString() {
        return String.format("[%s] IP=%-15s METHOD=%-7s STATUS=%-10s BYTES=%-8d URL=%s",
                timestamp, clientIp, method, status, bytes, url);
    }
}