//
// Diese Datei wurde mit der Eclipse Implementation of JAXB, v2.3.7 generiert
// Siehe https://eclipse-ee4j.github.io/jaxb-ri
// Änderungen an dieser Datei gehen bei einer Neukompilierung des Quellschemas verloren.
// Generiert: 2023.05.03 um 12:26:12 PM CEST
//


package info.rexs.db.jaxb;

import java.util.ArrayList;
import java.util.List;
import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlRootElement;
import jakarta.xml.bind.annotation.XmlType;


/**
 * <p>Java-Klasse für anonymous complex type.
 *
 * <p>Das folgende Schemafragment gibt den erwarteten Content an, der in dieser Klasse enthalten ist.
 *
 * <pre>
 * &lt;complexType&gt;
 *   &lt;complexContent&gt;
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType"&gt;
 *       &lt;sequence&gt;
 *         &lt;element ref="{}componentAttributeMapping" maxOccurs="unbounded" minOccurs="0"/&gt;
 *       &lt;/sequence&gt;
 *     &lt;/restriction&gt;
 *   &lt;/complexContent&gt;
 * &lt;/complexType&gt;
 * </pre>
 *
 *
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "", propOrder = {
    "componentAttributeMapping"
})
@XmlRootElement(name = "componentAttributeMappings")
public class ComponentAttributeMappings {

    protected List<ComponentAttributeMapping> componentAttributeMapping;

    /**
     * Gets the value of the componentAttributeMapping property.
     *
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the componentAttributeMapping property.
     *
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getComponentAttributeMapping().add(newItem);
     * </pre>
     *
     *
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link ComponentAttributeMapping }
     *
     *
     */
    public List<ComponentAttributeMapping> getComponentAttributeMapping() {
        if (componentAttributeMapping == null) {
            componentAttributeMapping = new ArrayList<ComponentAttributeMapping>();
        }
        return this.componentAttributeMapping;
    }

}