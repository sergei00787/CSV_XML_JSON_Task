package org.example;

import com.google.gson.*;
import com.google.gson.reflect.TypeToken;
import com.opencsv.CSVReader;
import com.opencsv.bean.ColumnPositionMappingStrategy;
import com.opencsv.bean.CsvToBean;
import com.opencsv.bean.CsvToBeanBuilder;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.*;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

public class Main {

    public static void main(String[] args) {
        String[] columnMapping = {"id", "firstName", "lastName", "country", "age"};
        String fileName = "data.csv";

        // Task 1 - CSV to JSON
        List<Employee> employeeList = parseCSV(columnMapping, fileName);
        String json = listToJson(employeeList);
        writeString(json, "data.json");

        // Task 2 - XML to JSON
        List<Employee> employeeList2 = parseXML("data.xml");
        String json2 = listToJson(employeeList2);
        writeString(json2, "data2.json");

        // Task3 - Json to List
        List<Employee> employeeList3 = jsonToList(readString("data.json"));
        employeeList3.stream().forEach(System.out::println);
    }

    public static List<Employee> parseCSV(String[] columnMapping, String fileName) {
        try (FileReader fileReader = new FileReader(fileName)) {
            CSVReader csvReader = new CSVReader(fileReader);

            ColumnPositionMappingStrategy mappingStrategy = new ColumnPositionMappingStrategy();
            mappingStrategy.setColumnMapping(columnMapping);
            mappingStrategy.setType(Employee.class);

            CsvToBeanBuilder csvToBeanBuilder = new CsvToBeanBuilder(csvReader);
            CsvToBean<Employee> csvToBean = csvToBeanBuilder.withMappingStrategy(mappingStrategy).build();
            return csvToBean.parse();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return new ArrayList<Employee>();
    }

    public static String listToJson(List<Employee> employees) {
        Type listType = TypeToken.getParameterized(List.class, Employee.class).getType();

        GsonBuilder gsonBuilder = new GsonBuilder();
        Gson gson = gsonBuilder.setFieldNamingStrategy(FieldNamingPolicy.LOWER_CASE_WITH_UNDERSCORES)
                .setPrettyPrinting()
                .create();

        return gson.toJson(employees, listType);
    }

    public static void writeString(String input, String fileName) {
        try (FileWriter fileWriter = new FileWriter(fileName)) {
            fileWriter.write(input);
            fileWriter.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static List<Employee> parseXML(String fileName) {
        DocumentBuilderFactory builderFactory = DocumentBuilderFactory.newInstance();
        DocumentBuilder builder = null;

        List<Employee> employees = new ArrayList<Employee>();

        try {
            File file = new File(fileName);
            builder = builderFactory.newDocumentBuilder();
            Document doc = builder.parse(file);

            Node root = doc.getDocumentElement();
            NodeList empNodes = root.getChildNodes();

            for (int i = 0; i < empNodes.getLength(); i++) {
                if (Node.ELEMENT_NODE == empNodes.item(i).getNodeType()) {
                    NodeList nodeListProperties = empNodes.item(i).getChildNodes();

                    int id = 0;
                    String firstName = "";
                    String lastName = "";
                    String country = "";
                    int age = 0;

                    for (int j = 0; j < nodeListProperties.getLength(); j++) {
                        if (Node.ELEMENT_NODE == nodeListProperties.item(j).getNodeType()) {
                            Element prop = (Element) nodeListProperties.item(j);

                            switch (prop.getNodeName()) {
                                case "id":
                                    id = Integer.parseInt(prop.getTextContent());
                                    break;
                                case "firstName":
                                    firstName = prop.getTextContent();
                                    break;
                                case "lastName":
                                    lastName = prop.getTextContent();
                                    break;
                                case "country":
                                    country = prop.getTextContent();
                                    break;
                                case "age":
                                    age = Integer.parseInt(prop.getTextContent());
                                    break;
                            }
                        }
                    }

                    Employee emp = new Employee(id, firstName, lastName, country, age);
                    employees.add(emp);
                }
            }

        } catch (ParserConfigurationException e) {
            e.printStackTrace();
        } catch (SAXException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return employees;

    }

    public static String readString(String fileName){
        try(BufferedReader bufferedReader = new BufferedReader(new FileReader(fileName))) {
            StringBuilder stringBuilder = new StringBuilder();
            bufferedReader.lines().forEach(str -> stringBuilder.append(str));
            return stringBuilder.toString();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static List<Employee> jsonToList(String strJson) {
        JSONParser parser = new JSONParser();
        List<Employee> employees = new ArrayList<>();
        try {
            JSONArray jsonArray = (JSONArray) parser.parse(strJson);

            GsonBuilder builder = new GsonBuilder();
            Gson gson = builder
                    .setFieldNamingStrategy(FieldNamingPolicy.LOWER_CASE_WITH_UNDERSCORES)
                    .create();

            jsonArray.forEach(json -> {
                Employee emp = gson.fromJson( json.toString(), Employee.class);
                employees.add(emp);
            });
        } catch (ParseException e) {
            e.printStackTrace();
        }

        return employees;

    }

}
