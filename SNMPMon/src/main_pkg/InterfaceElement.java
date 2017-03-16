package main_pkg;

// класс для характеристики элемента интерфейса
public class InterfaceElement 
{
	private String key;
	private String name;
	private String value;
	
	// конструктор
	public InterfaceElement(String key, String name)
	{
		this.key = key;
		this.name = name;
	}
	
	public String getKey() {
		return key;
	}
	public void setKey(String key) {
		this.key = key;
	}
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public String getValue() {
		return value;
	}
	public void setValue(String value) {
		this.value = value;
	}
	
	// функция, возвращающая строку типа "пара=значение"
	public String getNameValue()
	{
		return (this.name + "=\"" + this.value + "\" ");
	}
	
}
