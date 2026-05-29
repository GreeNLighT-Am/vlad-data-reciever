
package ru.vlad.vlad_data_receiver.model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlType;


/**
 * <p>Java class for documentCard complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="documentCard"&gt;
 *   &lt;complexContent&gt;
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType"&gt;
 *       &lt;sequence&gt;
 *         &lt;element name="VariableAttribute" type="{http://ru.vlad/documents}documentAttribute" maxOccurs="unbounded" minOccurs="0"/&gt;
 *       &lt;/sequence&gt;
 *     &lt;/restriction&gt;
 *   &lt;/complexContent&gt;
 * &lt;/complexType&gt;
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "documentCard", propOrder = {
    "variableAttribute"
})
public class DocumentCard
    implements Serializable
{

    private final static long serialVersionUID = 1L;
    @XmlElement(name = "VariableAttribute")
    protected List<DocumentAttribute> variableAttribute;

    /**
     * Gets the value of the variableAttribute property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the Jakarta XML Binding object.
     * This is why there is not a <CODE>set</CODE> method for the variableAttribute property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getVariableAttribute().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link DocumentAttribute }
     * 
     * 
     */
    public List<DocumentAttribute> getVariableAttribute() {
        if (variableAttribute == null) {
            variableAttribute = new ArrayList<DocumentAttribute>();
        }
        return this.variableAttribute;
    }

}
