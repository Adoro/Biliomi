package nl.juraji.biliomi.model.chat;

import nl.juraji.biliomi.model.core.User;
import nl.juraji.biliomi.utility.types.hibernatetypes.DateTimeISO8601Type;
import org.hibernate.annotations.Type;
import org.joda.time.DateTime;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * Created by Juraji on 13-5-2017.
 * Biliomi v3
 */
@Entity
@XmlRootElement(name = "ModerationRecord")
@XmlAccessorType(XmlAccessType.FIELD)
public class ModerationRecord {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @XmlElement(name = "Id")
  private long id;

  @NotNull
  @ManyToOne
  @XmlElement(name = "User")
  private User user;

  @Column
  @NotNull
  @Enumerated(EnumType.STRING)
  @XmlElement(name = "Action")
  private ModerationAction action;

  @Column
  @NotNull
  @Enumerated(EnumType.STRING)
  @XmlElement(name = "Reason")
  private ModerationReason reason;

  @Column
  @XmlElement(name = "Message")
  private String message;

  @Column
  @Type(type = DateTimeISO8601Type.TYPE)
  @XmlElement(name = "Date")
  private DateTime date;

  public long getId() {
    return id;
  }

  public void setId(long id) {
    this.id = id;
  }

  public User getUser() {
    return user;
  }

  public void setUser(User user) {
    this.user = user;
  }

  public ModerationAction getAction() {
    return action;
  }

  public void setAction(ModerationAction action) {
    this.action = action;
  }

  public ModerationReason getReason() {
    return reason;
  }

  public void setReason(ModerationReason reason) {
    this.reason = reason;
  }

  public String getMessage() {
    return message;
  }

  public void setMessage(String message) {
    this.message = message;
  }

  public DateTime getDate() {
    return date;
  }

  public void setDate(DateTime date) {
    this.date = date;
  }
}
