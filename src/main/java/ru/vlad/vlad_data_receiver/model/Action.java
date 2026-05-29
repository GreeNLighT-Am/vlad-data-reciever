
package ru.vlad.vlad_data_receiver.model;

import java.io.Serializable;
import java.time.LocalDateTime;
import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlType;
import jakarta.xml.bind.annotation.adapters.XmlJavaTypeAdapter;
import ru.vlad.vlad_data_receiver.adapter.DotSeparatorDateAdapter;


/**
 * <p>Java class for action complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="action"&gt;
 *   &lt;complexContent&gt;
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType"&gt;
 *       &lt;sequence&gt;
 *         &lt;element name="ActionNumber" type="{http://www.w3.org/2001/XMLSchema}int"/&gt;
 *         &lt;element name="Name" type="{http://www.w3.org/2001/XMLSchema}string"/&gt;
 *         &lt;element name="Role" type="{http://www.w3.org/2001/XMLSchema}string"/&gt;
 *         &lt;element name="Action" type="{http://www.w3.org/2001/XMLSchema}string"/&gt;
 *         &lt;element name="Date" type="{http://ru.vlad/documents}dotSeparatorDate"/&gt;
 *       &lt;/sequence&gt;
 *     &lt;/restriction&gt;
 *   &lt;/complexContent&gt;
 * &lt;/complexType&gt;
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "action", propOrder = {
    "actionNumber",
    "name",
    "role",
    "action",
    "date"
})
public class Action
    implements Serializable
{

    private final static long serialVersionUID = 1L;
    @XmlElement(name = "ActionNumber")
    protected int actionNumber;
    @XmlElement(name = "Name", required = true)
    protected String name;
    @XmlElement(name = "Role", required = true)
    protected String role;
    @XmlElement(name = "Action", required = true)
    protected String action;
    @XmlElement(name = "Date", required = true, type = String.class)
    @XmlJavaTypeAdapter(DotSeparatorDateAdapter.class)
    protected LocalDateTime date;

    /**
     * Gets the value of the actionNumber property.
     * 
     */
    public int getActionNumber() {
        return actionNumber;
    }

    /**
     * Sets the value of the actionNumber property.
     * 
     */
    public void setActionNumber(int value) {
        this.actionNumber = value;
    }

    /**
     * Gets the value of the name property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getName() {
        return name;
    }

    /**
     * Sets the value of the name property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setName(String value) {
        this.name = value;
    }

    /**
     * Gets the value of the role property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getRole() {
        return role;
    }

    /**
     * Sets the value of the role property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setRole(String value) {
        this.role = value;
    }

    /**
     * Gets the value of the action property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getAction() {
        return action;
    }

    /**
     * Sets the value of the action property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setAction(String value) {
        this.action = value;
    }

    /**
     * Gets the value of the date property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public LocalDateTime getDate() {
        return date;
    }

    /**
     * Sets the value of the date property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setDate(LocalDateTime value) {
        this.date = value;
    }

}
