
package ru.vlad.vlad_data_receiver.model;

import java.io.Serializable;
import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlType;


/**
 * <p>Java class for parentDocs complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="parentDocs"&gt;
 *   &lt;complexContent&gt;
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType"&gt;
 *       &lt;sequence&gt;
 *         &lt;element name="ParentDocsNumber" type="{http://www.w3.org/2001/XMLSchema}string"/&gt;
 *         &lt;element name="ParentDocsListNumber" type="{http://www.w3.org/2001/XMLSchema}string"/&gt;
 *       &lt;/sequence&gt;
 *     &lt;/restriction&gt;
 *   &lt;/complexContent&gt;
 * &lt;/complexType&gt;
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "parentDocs", propOrder = {
    "parentDocsNumber",
    "parentDocsListNumber"
})
public class ParentDocs
    implements Serializable
{

    private final static long serialVersionUID = 1L;
    @XmlElement(name = "ParentDocsNumber", required = true)
    protected String parentDocsNumber;
    @XmlElement(name = "ParentDocsListNumber", required = true)
    protected String parentDocsListNumber;

    /**
     * Gets the value of the parentDocsNumber property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getParentDocsNumber() {
        return parentDocsNumber;
    }

    /**
     * Sets the value of the parentDocsNumber property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setParentDocsNumber(String value) {
        this.parentDocsNumber = value;
    }

    /**
     * Gets the value of the parentDocsListNumber property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getParentDocsListNumber() {
        return parentDocsListNumber;
    }

    /**
     * Sets the value of the parentDocsListNumber property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setParentDocsListNumber(String value) {
        this.parentDocsListNumber = value;
    }

}
