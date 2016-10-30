package hello;

import java.io.*;
import java.net.URLDecoder;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.logging.Logger;

import hello.dto.Classificator;
import hello.dto.ClassificatorUnited;
import hello.jdbc.ConfigJdbc;
import hello.jdbc.NamedParameterStatement;
import lombok.SneakyThrows;
import org.springframework.beans.propertyeditors.CustomDateEditor;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.*;

@CrossOrigin(origins = "http://localhost:4200")
@RestController
@RequestMapping(value = "/classificators")

public class RestService {

    public static void main(String[] a) throws Exception {
        String sqlQuery = "select * from okpd where level = 2 order by kod";
        try (final Connection db = configJdbc().getConnection()) {
            try (final PreparedStatement stmt = db.prepareStatement(sqlQuery)) {
                try (ResultSet resultSet = stmt.executeQuery()) {
                    while (resultSet.next()) {
                        String q = "update okpd set parent_kod = '" + resultSet.getString("kod") + "' where id = '" + resultSet.getString("id") + "';";
                        System.out.println(q);


                    }
                }
            }
        }

    }

    public static void fill() throws Exception {
        String sqlQuery = "select * from okpd where length(replace(kod, '.', '')) = 2  order by kod";
        File statText = new File("statsTest.sql");
        FileWriter fw = new FileWriter(statText);
        BufferedWriter bw = new BufferedWriter(fw);
        try (final Connection db = configJdbc().getConnection()) {
            try (final PreparedStatement stmt = db.prepareStatement(sqlQuery)) {
                try (ResultSet resultSet = stmt.executeQuery()) {
                    while (resultSet.next()) {
                        System.out.println(resultSet.getString("kod"));
                        fillParent(resultSet.getString("id"), resultSet.getString("clear_kod"), db, bw);
                    }
                }
            }
        }
        bw.close();
        fw.close();

    }

    static void fillParent(String parentId, String parentCode, Connection db, BufferedWriter bw) throws Exception {



        String query = "select * from okpd where clear_kod like '" + parentCode +
                "%' and level = " +  String.valueOf(parentCode.length() + 1) +  "  order by kod";
        try (final PreparedStatement stmt = db.prepareStatement(query)) {
            try (ResultSet resultSet = stmt.executeQuery()) {
                while (resultSet.next()) {
                    String uQuery = "update okpd set parent_id = '" + parentId + "', parent_kod = '" + parentCode +"' where id = '" + resultSet.getString("id") + "';";
                    bw.append(uQuery);
                    bw.newLine();
                    System.out.println(uQuery);
                   // db.createStatement().executeUpdate(uQuery);
                    System.out.println("parent:" + parentCode +  "  > " + resultSet.getString("kod"));
                    fillParent(resultSet.getString("id"), resultSet.getString("clear_kod"), db, bw);
                }
            }
        }

    }

    private static final Logger logger = Logger.getLogger(RestService.class.getName());


    @InitBinder
    public void initBinder(WebDataBinder binder) {
        binder.registerCustomEditor(Date.class,
                new CustomDateEditor(new SimpleDateFormat("yyyy-MM-dd"), true, 10));
    }


    static ConfigJdbc configJdbc() {
        return new ConfigJdbc(
                org.postgresql.Driver.class,
                "jdbc:postgresql://localhost:5432/okpd",
                "postgres",
                "12345");
    }

    @RequestMapping("")
    public List<ClassificatorUnited> fullTextSearch(@RequestParam(value = "query", required = true) String query) throws Exception {
        query = URLDecoder.decode(query, "UTF-8");
        logger.info("fullTextSearch:" + query);
        List<ClassificatorUnited> result = new ArrayList<>();
        try (final Connection db = configJdbc().getConnection()) {
            String sqlQuery = "select * from okpd where name like '% " + query +  "%' order by kod";
            try (final PreparedStatement stmt = db.prepareStatement(sqlQuery)) {
                try (ResultSet resultSet = stmt.executeQuery()) {
                    while (resultSet.next()) {
                        result.add(classificatorUnited(resultSet));
                    }
                }
            }
        }
        return result;
    }

    @RequestMapping("/okpd")
    public List<Classificator> okpd() throws Exception {
       return okpdTree(null);
    }

    @RequestMapping("/okpd/{parentId}")
    public List<Classificator> okpdTree(@PathVariable(value="parentId") String code) throws Exception {
        List<Classificator> result = new ArrayList<>();
        try (final Connection db = configJdbc().getConnection()) {
            String query;
            if (code == null) {
                query = "select * from okpd where parent_id is null order by kod";
            } else {
                query = "select * from okpd where parent_kod = '" + code + "' order by kod";
            }
            logger.info("query:" + query);
            try (final PreparedStatement sql = db.prepareStatement(query)) {
                try (ResultSet resultSet = sql.executeQuery()) {
                    while (resultSet.next()) {
                        result.add(classificator(resultSet));
                    }
                }
            }
        }
        return result;
    }


    @SneakyThrows
    private Classificator okpdBy(String code, Connection db) {
        String query = "select * from okpd where replace(kod, '.', '') = :code";
        try (final NamedParameterStatement stmt = new NamedParameterStatement(db, query)) {
            stmt.setString("code", code);
            try (ResultSet resultSet = stmt.executeQuery()) {
                if (resultSet.next()) {
                    return classificator(resultSet);
                } else {
                    return null;
                }
            }
        }
    }


    private Classificator classificator(ResultSet resultSet) throws SQLException {
        Classificator classificator = new Classificator();
        classificator.setCode(resultSet.getString("kod"));
        classificator.setName(resultSet.getString("name"));
        classificator.setNotes(resultSet.getString("notes"));
        int level;
        if (resultSet.getString("subkod2") == null) {
            level = 1;
        } else if (resultSet.getString("subkod3") == null) {
            level = 2;
        } else if (resultSet.getString("subkod4") == null) {
            level = 3;
        } else {
            level = 4;
        }
        classificator.setLevel(level);
        return classificator;
    }

    private ClassificatorUnited classificatorUnited(ResultSet resultSet) throws SQLException {
        ClassificatorUnited classificatorUnited = new ClassificatorUnited();
        Classificator okpd = new Classificator();
        okpd.setCode(resultSet.getString("kod"));
        okpd.setName(resultSet.getString("name"));
        okpd.setNotes(resultSet.getString("notes"));
        int level;
        if (resultSet.getString("subkod2") == null) {
            level = 1;
        } else if (resultSet.getString("subkod3") == null) {
            level = 2;
        } else if (resultSet.getString("subkod4") == null) {
            level = 3;
        } else {
            level = 4;
        }
        okpd.setLevel(level);
        classificatorUnited.setOkpd(okpd);
        return classificatorUnited;
    }


}

