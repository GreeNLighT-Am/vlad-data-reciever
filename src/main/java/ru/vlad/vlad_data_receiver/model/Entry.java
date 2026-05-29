
package ru.vlad.vlad_data_receiver.model;

import java.io.Serializable;
import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlType;


/**
 * <p>Java class for entry complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="entry"&gt;
 *   &lt;complexContent&gt;
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType"&gt;
 *       &lt;sequence&gt;
 *         &lt;element name="EntryNumber" type="{http://www.w3.org/2001/XMLSchema}int"/&gt;
 *         &lt;element name="AccountNumberDT" type="{http://www.w3.org/2001/XMLSchema}string"/&gt;
 *         &lt;element name="AccountNumberKT" type="{http://www.w3.org/2001/XMLSchema}string"/&gt;
 *         &lt;element name="EntrySum" type="{http://ru.vlad/documents}entrySum"/&gt;
 *       &lt;/sequence&gt;
 *     &lt;/restriction&gt;
 *   &lt;/complexContent&gt;
 * &lt;/complexType&gt;
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "entry", propOrder = {
    "entryNumber",
    "accountNumberDT",
    "accountNumberKT",
    "entrySum"
})
public class Entry
    implements Serializable
{

    private final static long serialVersionUID = 1L;
    @XmlElement(name = "EntryNumber")
    protected int entryNumber;
    @XmlElement(name = "AccountNumberDT", required = true)
    protected String accountNumberDT;
    @XmlElement(name = "AccountNumberKT", required = true)
    protected String accountNumberKT;
    @XmlElement(name = "EntrySum", required = true)
    protected EntrySum entrySum;

    /**
     * Gets the value of the entryNumber property.
     * 
     */
    public int getEntryNumber() {
        return entryNumber;
    }

    /**
     * Sets the value of the entryNumber property.
     * 
     */
    public void setEntryNumber(int value) {
        this.entryNumber = value;
    }

    /**
     * Gets the value of the accountNumberDT property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getAccountNumberDT() {
        return accountNumberDT;
    }

    /**
     * Sets the value of the accountNumberDT property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setAccountNumberDT(String value) {
        this.accountNumberDT = value;
    }

    /**
     * Gets the value of the accountNumberKT property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getAccountNumberKT() {
        return accountNumberKT;
    }

    /**
     * Sets the value of the accountNumberKT property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setAccountNumberKT(String value) {
        this.accountNumberKT = value;
    }

    /**
     * Gets the value of the entrySum property.
     * 
     * @return
     *     possible object is
     *     {@link EntrySum }
     *     
     */
    public EntrySum getEntrySum() {
        return entrySum;
    }

    /**
     * Sets the value of the entrySum property.
     * 
     * @param value
     *     allowed object is
     *     {@link EntrySum }
     *     
     */
    public void setEntrySum(EntrySum value) {
        this.entrySum = value;
    }

}
