
package ru.vlad.vlad_data_receiver.model;

import java.io.Serializable;
import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlAttribute;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlType;


/**
 * <p>Java class for sign complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="sign"&gt;
 *   &lt;complexContent&gt;
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType"&gt;
 *       &lt;sequence&gt;
 *         &lt;element name="SignCard" type="{http://ru.vlad/documents}documentCard"/&gt;
 *         &lt;element name="SignData" type="{http://ru.vlad/documents}signBody"/&gt;
 *       &lt;/sequence&gt;
 *       &lt;attribute name="CriptoType" use="required" type="{http://www.w3.org/2001/XMLSchema}string" /&gt;
 *       &lt;attribute name="Included" use="required" type="{http://www.w3.org/2001/XMLSchema}boolean" /&gt;
 *     &lt;/restriction&gt;
 *   &lt;/complexContent&gt;
 * &lt;/complexType&gt;
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "sign", propOrder = {
    "signCard",
    "signData"
})
public class Sign
    implements Serializable
{

    private final static long serialVersionUID = 1L;
    @XmlElement(name = "SignCard", required = true)
    protected DocumentCard signCard;
    @XmlElement(name = "SignData", required = true)
    protected SignBody signData;
    @XmlAttribute(name = "CriptoType", required = true)
    protected String criptoType;
    @XmlAttribute(name = "Included", required = true)
    protected boolean included;

    /**
     * Gets the value of the signCard property.
     * 
     * @return
     *     possible object is
     *     {@link DocumentCard }
     *     
     */
    public DocumentCard getSignCard() {
        return signCard;
    }

    /**
     * Sets the value of the signCard property.
     * 
     * @param value
     *     allowed object is
     *     {@link DocumentCard }
     *     
     */
    public void setSignCard(DocumentCard value) {
        this.signCard = value;
    }

    /**
     * Gets the value of the signData property.
     * 
     * @return
     *     possible object is
     *     {@link SignBody }
     *     
     */
    public SignBody getSignData() {
        return signData;
    }

    /**
     * Sets the value of the signData property.
     * 
     * @param value
     *     allowed object is
     *     {@link SignBody }
     *     
     */
    public void setSignData(SignBody value) {
        this.signData = value;
    }

    /**
     * Gets the value of the criptoType property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getCriptoType() {
        return criptoType;
    }

    /**
     * Sets the value of the criptoType property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setCriptoType(String value) {
        this.criptoType = value;
    }

    /**
     * Gets the value of the included property.
     * 
     */
    public boolean isIncluded() {
        return included;
    }

    /**
     * Sets the value of the included property.
     * 
     */
    public void setIncluded(boolean value) {
        this.included = value;
    }

}
