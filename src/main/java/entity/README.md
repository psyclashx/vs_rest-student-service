### Student

Die Annotation [`@XmlRootElement`](https://docs.oracle.com/javase/10/docs/api/javax/xml/bind/annotation/XmlRootElement.html) markiert eine Klasse als sogenannte *Top-Level-Klasse*, das heißt, sie kann als "standalone" XML-Dokument verwendet werden.

[`@XmlAccessorType`](https://docs.oracle.com/javase/10/docs/api/javax/xml/bind/annotation/XmlAccessorType.html) legt dabei fest, welche *Fields (Attribute)* bzw. *Properties (Getter-Setter-Pärchen)* als Elemente des XML-Dokuments übertragen werden.  
- `XmlAccessType.PROPERTY`: Alle Getter-Setter-Pärchen werden automatisch auf XML gebunden
- `XmlAccessType.FIELD`: Alle nicht statischen, nicht transienten Felder werden automatisch auf XML gebunden
- `XmlAccessType.PUBLIC_MEMBER`: Nur public-Attribute werden auf XML gebunden

[@XmlAttribute](https://docs.oracle.com/javase/10/docs/api/javax/xml/bind/annotation/XmlAttribute.html) legt fest, dass ein einfaches Attribut (String, Zahlen, Boolean) als Attribut eines XML-Elements statt eines eigenen XML-Elements dargestellt werden soll
Beispiel: `<student id="123></student>` statt `<student><id>123</id></student>`

Ein Konstruktor ohne Parameter ist immer zwingend erforderlich, da beim XML- und JSON-Binding zunächst immer erst ein neues Objekt (ohne Daten) erzeugt wird und dann anschließend alle Properties/Felder einzeln gesetzt werden.

### Adresse

Hier ist kein `@XmlRootElement` notwendig, da es nicht als alleinstehende XML-Dokument verwendet werden soll (es wird immer Teil eines Student-Objekts sein und nie "standalone" versendet werden).

Da hier `XmlAccessType.FIELD` gewählt wurde, sind (für das XML-Binding allein) keine Getter und Setter notwendig.