//
// Diese Datei wurde mit der JavaTM Architecture for XML Binding(JAXB) Reference Implementation, v2.2.11 generiert 
// Siehe <a href="http://java.sun.com/xml/jaxb">http://java.sun.com/xml/jaxb</a> 
// Änderungen an dieser Datei gehen bei einer Neukompilierung des Quellschemas verloren. 
// Generiert: 2020.08.19 um 03:16:47 PM CEST 
//


package info.rexs.db.jaxb;

import jakarta.xml.bind.annotation.XmlRegistry;


/**
 * This object contains factory methods for each 
 * Java content interface and Java element interface 
 * generated in the info.rexs.db.jaxb package. 
 * <p>An ObjectFactory allows you to programatically 
 * construct new instances of the Java representation 
 * for XML content. The Java representation of XML 
 * content can consist of schema derived interfaces 
 * and classes representing the binding of schema 
 * type definitions, element declarations and model 
 * groups.  Factory methods for each of these are 
 * provided in this class.
 * 
 */
@XmlRegistry
public class ObjectFactory {


    /**
     * Create a new ObjectFactory that can be used to create new instances of schema derived classes for package: info.rexs.db.jaxb
     * 
     */
    public ObjectFactory() {
    }

    /**
     * Create an instance of {@link RexsDatabaseModelFile}
     * 
     */
    public RexsDatabaseModelFile createRexsModel() {
        return new RexsDatabaseModelFile();
    }

    /**
     * Create an instance of {@link Units }
     * 
     */
    public Units createUnits() {
        return new Units();
    }

    /**
     * Create an instance of {@link Unit }
     * 
     */
    public Unit createUnit() {
        return new Unit();
    }

    /**
     * Create an instance of {@link ValueTypes }
     * 
     */
    public ValueTypes createValueTypes() {
        return new ValueTypes();
    }

    /**
     * Create an instance of {@link ValueType }
     * 
     */
    public ValueType createValueType() {
        return new ValueType();
    }

    /**
     * Create an instance of {@link Components }
     * 
     */
    public Components createComponents() {
        return new Components();
    }

    /**
     * Create an instance of {@link Component }
     * 
     */
    public Component createComponent() {
        return new Component();
    }

    /**
     * Create an instance of {@link Attributes }
     * 
     */
    public Attributes createAttributes() {
        return new Attributes();
    }

    /**
     * Create an instance of {@link Attribute }
     * 
     */
    public Attribute createAttribute() {
        return new Attribute();
    }

    /**
     * Create an instance of {@link EnumValues }
     * 
     */
    public EnumValues createEnumValues() {
        return new EnumValues();
    }

    /**
     * Create an instance of {@link EnumValue }
     * 
     */
    public EnumValue createEnumValue() {
        return new EnumValue();
    }

    /**
     * Create an instance of {@link ComponentAttributeMappings }
     * 
     */
    public ComponentAttributeMappings createComponentAttributeMappings() {
        return new ComponentAttributeMappings();
    }

    /**
     * Create an instance of {@link ComponentAttributeMapping }
     * 
     */
    public ComponentAttributeMapping createComponentAttributeMapping() {
        return new ComponentAttributeMapping();
    }

}
