package com.tmall.Dao;

import com.tmall.bean.Category;
import com.tmall.bean.Product;
import com.tmall.bean.ProductImage;
import com.tmall.bean.Review;
import com.tmall.util.DBUtil;
import com.tmall.util.DateUtil;

import java.util.ArrayList;
import java.util.Date;

import java.sql.*;
import java.util.List;

public class ProductDAO {

    public int getTotal(int cid) {
        int total =0;
        try (Connection c = DBUtil.getConnection(); Statement s = c.createStatement()) {
            String sql = "SELECT COUNT(*) FROM product WHERE cid =" + cid;
            ResultSet rs = s.executeQuery(sql);
            while (rs.next()) {
                total = rs.getInt(1);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return total;
    }

    public void add(Product bean) {
        String sql = "INSERT INTO product VALUES(null,?,?,?,?,?,?,?)";
        try (Connection c = DBUtil.getConnection(); PreparedStatement ps = c.prepareStatement(sql)) {

            ps.setString(1, bean.getName());
            ps.setString(2, bean.getSubTitle());
            ps.setFloat(3, bean.getOriginalPrice());
            ps.setFloat(4, bean.getPromotePrice());
            ps.setInt(5, bean.getStock());
            ps.setInt(6, bean.getCategory().getId());
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

    public void update(Product bean) {
        String sql = "UPDATE product SET name=?,subtitle=?, orignal_price=?,promote_price=?,stock=?,cid=?,create_date=? WHERE id=? ";
        try (Connection c = DBUtil.getConnection(); PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, bean.getName());
            ps.setString(2, bean.getSubTitle());
            ps.setFloat(3, bean.getOriginalPrice());
            ps.setFloat(4, bean.getPromotePrice());
            ps.setInt(5, bean.getStock());
            ps.setInt(6, bean.getCategory().getId());
            ps.setTimestamp(7, DateUtil.d2t(bean.getCreateDate()));
            ps.setInt(8, bean.getId());
            ps.execute();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void delete(int id) {
        try (Connection c = DBUtil.getConnection(); Statement s = c.createStatement()) {
            String sql = "DELETE FROM product WHERE id ="+id;
            s.execute(sql);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public Product get(int id) {
        Product bean = new Product();
        try (Connection c = DBUtil.getConnection(); Statement s = c.createStatement()) {
            String sql = "SELECT * FROM product WHERE id =" + id;
            ResultSet rs = s.executeQuery(sql);
            while (rs.next()) {
                String name = rs.getString("name");
                String subTitle = rs.getString("subTitle");
                float originalPrice = rs.getFloat("originalPrice");
                float promotePrice = rs.getFloat("promotePrice");
                int stock = rs.getInt("stock");
                int cid = rs.getInt("cid");
                Date createDate = DateUtil.t2d(rs.getTimestamp("create_date"));

                bean.setName(name);
                bean.setSubTitle(subTitle);
                bean.setOriginalPrice(originalPrice);
                bean.setPromotePrice(promotePrice);
                bean.setStock(stock);
                bean.setCategory(new CategoryDAO().get(cid));
                bean.setCreateDate(createDate);
                bean.setId(id);
                setFirstProductImage(bean);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return bean;
    }

    public List<Product> list(int cid, int start, int count) {
        List<Product> beans = new ArrayList<>();
        Category category = new CategoryDAO().get(cid);
        String sql = "SELECT * FROM product WHERE cid = ? ORDER BY id DESC limit ?,?";
        try (Connection c = DBUtil.getConnection(); PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, cid);
            ps.setInt(1, start);
            ps.setInt(3, count);
            ResultSet rs = ps.executeQuery();

            while (rs.next()) {
                Product bean = new Product();
                int id = rs.getInt(1);
                String name = rs.getString("name");
                String subTitle = rs.getString("subTitle");
                float originalPrice = rs.getFloat("originalPrice");
                float promotePrice = rs.getFloat("promotePrice");
                int stock = rs.getInt("stock");

                Date createDate = DateUtil.t2d(rs.getTimestamp("create_date"));

                bean.setName(name);
                bean.setSubTitle(subTitle);
                bean.setOriginalPrice(originalPrice);
                bean.setPromotePrice(promotePrice);
                bean.setStock(stock);
                bean.setCategory(category);
                bean.setCreateDate(createDate);
                bean.setId(id);
                setFirstProductImage(bean);
                beans.add(bean);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return beans;
    }

    public List<Product> list(int cid) {
        return list(cid,0, Short.MAX_VALUE);
    }

    public List<Product> list(int start, int count) {
        List<Product> beans = new ArrayList<>();
        String sql = "SELECT * FROM Product limit ?,?";
        try (Connection c = DBUtil.getConnection(); PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, start);
            ps.setInt(2, count);

            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                Product bean = new Product();
                int id = rs.getInt(1);
                int cid = rs.getInt("cid");
                String name = rs.getString("name");
                String subTitle = rs.getString("subTitle");
                float originalPrice = rs.getFloat("originalPrice");
                float promotePrice = rs.getFloat("promotePrice");
                int stock = rs.getInt("stock");

                Date createDate = DateUtil.t2d(rs.getTimestamp("create_date"));

                bean.setName(name);
                bean.setSubTitle(subTitle);
                bean.setOriginalPrice(originalPrice);
                bean.setPromotePrice(promotePrice);
                bean.setStock(stock);
                bean.setCategory(new CategoryDAO().get(cid));
                bean.setCreateDate(createDate);
                bean.setId(id);
                setFirstProductImage(bean);
                beans.add(bean);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void fill(List<Category> categoryList) {
        for (Category category : categoryList) {
            fill(category);
        }
    }

    public void fill(Category category) {
        List<Product> ps = this.list(category.getId());
        category.setProducts(ps);
    }

    public void fillByRow(List<Category> categoryList) {
        int productNumberEachRow = 8;
        for (Category category : categoryList) {
            List<Product> products = category.getProducts();
            List<List<Product>> productsByRow = new ArrayList<>();
            for (int i = 0; i < products.size(); i += productNumberEachRow) {
                int size = i+productNumberEachRow;
                size = size > products.size() ? products.size() : size;
                List<Product> productsOfEachRow = products.subList(i, size);
                productsByRow.add(productsOfEachRow);
            }
            category.setProductsByRow(productsByRow);
        }
    }

    public void setFirstProductImage(Product p) {
        List<ProductImage> productImages = new ProductImageDAO().list(p, ProductImageDAO.type_single);
        if (!productImages.isEmpty()) {
            p.setFirstProductImage(productImages.get(0));
        }
     }

    public void setSaleAndReviewNumber(Product p) {
        int saleCount = new OrderItemDao().getSaleCount(p.getId());
        p.setSaleCount(saleCount);

        int reviewCount = new ReviewDao().getCount(p.getId());
        p.setReviewCount(reviewCount);
    }

    public void setSaleAndReviewNumber(List<Product> products) {
        for (Product p : products) {
            setSaleAndReviewNumber(p);
        }
    }

    public List<Product> search(String keyword, int start, int count) {
        List<Product> beans = new ArrayList<>();
        if (null == keyword || 0 == keyword.trim().length()) {
            return beans;
        }

        String sql = "SELECT * FROM product WHERE name LIKE ? ,limit ?,?";
        try (Connection c = DBUtil.getConnection(); PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, keyword);
            ps.setInt(2, start);
            ps.setInt(3, count);
            ResultSet rs = ps.executeQuery();

            while (rs.next()) {
                Product bean = new Product();
                int id = rs.getInt(1);
                int cid = rs.getInt("cid");
                String name = rs.getString("name");
                String subTitle = rs.getString("subTitle");
                float originalPrice = rs.getFloat("original_price");
                float promotePrice = rs.getFloat("promote_price");
                int stock = rs.getInt("stock");
                Date createDate = DateUtil.t2d(rs.getTimestamp("create_date"));

                bean.setName(name);
                bean.setSubTitle(subTitle);
                bean.setOriginalPrice(originalPrice);
                bean.setPromotePrice(promotePrice);
                bean.setStock(stock);
                bean.setCreateDate(createDate);
                bean.setId(id);
                bean.setCategory(new CategoryDAO().get(cid));
                setFirstProductImage(bean);
                beans.add(bean);
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
        return beans;
    }
}
