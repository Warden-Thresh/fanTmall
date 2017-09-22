package com.tmall.Dao;

import com.tmall.bean.Product;
import com.tmall.bean.Property;
import com.tmall.bean.PropertyValue;
import com.tmall.util.DBUtil;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class PropertyValueDAO {
    public int getTotal() {
        int total = 0;
        try (Connection c = DBUtil.getConnection(); Statement s = c.createStatement()) {
            String sql = "select count(*) from property_value";
            ResultSet rs = s.executeQuery(sql);

            while (rs.next()) {
                total = rs.getInt(1);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return total;
    }

    public void add(PropertyValue bean) {
        String sql = "insert into property_value values(null,?,?,?)";
        try (Connection c = DBUtil.getConnection(); PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, bean.getProduct().getId());
            ps.setInt(2, bean.getProperty().getId());
            ps.setString(3, bean.getValue());
            ps.execute();

            ResultSet rs = ps.getGeneratedKeys();
            if (rs.next()) {
                int id = rs.getInt(1);
                bean.setId(id);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void update(PropertyValue bean) {
        String sql = "UPDATE property_value set id=?,ptid=?,value=? where id =?";
        try (Connection c = DBUtil.getConnection(); PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, bean.getProduct().getId());
            ps.setInt(2, bean.getProperty().getId());
            ps.setString(3, bean.getValue());
            ps.setInt(4, bean.getId());
            ps.execute();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void delete(int id) {
        try (Connection c = DBUtil.getConnection(); Statement s = c.createStatement()) {
            String sql = "delete from property_value where id = " + id;
            s.execute(sql);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public PropertyValue get(int id) {
        PropertyValue bean = new PropertyValue();
        try (Connection c = DBUtil.getConnection(); Statement s = c.createStatement()) {
            String sql = "select * from property_value where id =" + id;
            ResultSet rs = s.executeQuery(sql);
            if (rs.next()) {
                int pid = rs.getInt("pid");
                int ptid = rs.getInt("ptid");
                String value = rs.getString("value");

                Product product = new ProductDAO.get(pid);
                Property property = new PropertyDAO().get(ptid);
                bean.setId(id);
                bean.setValue(value);
                bean.setProduct(product);
                bean.setProperty(property);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return bean;
    }

    public PropertyValue get(int ptid, int pid) {
        PropertyValue bean = null;
        try (Connection c = DBUtil.getConnection(); Statement s = c.createStatement()) {
            String sql = "SELECT * FROM property_value WHERE ptid =" + ptid + "and pid=" + pid;
            ResultSet rs = s.executeQuery(sql);
            if (rs.next()) {
                bean = new PropertyValue();
                int id = rs.getInt("id");
                String value = rs.getString("value");
                Product product = new ProductDAO().get(pid);
                Property property = new PropertyDAO().get(ptid);
                bean.setProperty(property);
                bean.setProduct(product);
                bean.setValue(value);
                bean.setId(id);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return bean;
    }

    public List<PropertyValue> list() {
        return list(0, Short.MAX_VALUE);
    }

    public List<PropertyValue> list(int start, int count) {
        List<PropertyValue> beans = new ArrayList<>();
        String sql = "SELECT * FROM property_vale ORDER BY id DESC limit ?,?";
        try (Connection c = DBUtil.getConnection(); PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, start);
            ps.setInt(2, count);
            ResultSet rs = ps.executeQuery();
            beans = getProperty(rs);
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return beans;
    }

    public List<PropertyValue> list(int pid) {
        List<PropertyValue> beans = new ArrayList<>();

        String sql = "SELECT * FROM property_value WHERE pid =? ORDER BY ptid DESC";

        try (Connection c = DBUtil.getConnection(); PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, pid);
            ResultSet rs = ps.executeQuery();
            beans = getProperty(rs);
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return beans;
    }

    public void init(Product p) {
        List<Property> pts = new PropertyDAO().list(p.getCategory().getId());

        for (Property pt : pts) {
            PropertyValue pv = get(pt.getId(), p.getId());
            if (null == pv) {
                pv = new PropertyValue();
                pv.setProduct(p);
                pv.setProperty(pt);
                this.add(pv);
            }
        }
    }

    private List<PropertyValue> getProperty(ResultSet rs) {
        List<PropertyValue> beans = new ArrayList<>();
        try {
            while (rs.next()) {
                PropertyValue bean = new PropertyValue();
                int id = rs.getInt("id");
                int pid = rs.getInt("pid");
                int ptid = rs.getInt("ptid");
                String value = rs.getString("value");
                Product product = new ProductDAO().get(pid);
                Property property = new PropertyDAO().get(ptid);
                bean.setId(id);
                bean.setProduct(product);
                bean.setProperty(property);
                bean.setValue(value);
                beans.add(bean);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return beans;
    }

}
