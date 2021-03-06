package org.thonk.ejb;

import java.util.logging.Logger;
import javax.ejb.Stateless;
import java.net.UnknownHostException;
import java.util.List;
import java.util.ArrayList;
import javax.annotation.PostConstruct;

import com.mongodb.MongoClient;
import com.mongodb.DB;
import com.mongodb.ServerAddress;
import com.mongodb.DBCollection;
import org.mongojack.DBUpdate;

import org.mongojack.DBRef;
import org.mongojack.JacksonDBCollection;
import org.mongojack.WriteResult;

import org.thonk.entities.*;

@Stateless
public class MongoBean {

    private final static Logger log = Logger.getLogger(MongoBean.class.getName());

    MongoClient mongo;
    DB db; 

    @PostConstruct
    void init() {
          try {
              mongo = new MongoClient(new ServerAddress("localhost", 27017));
              db = mongo.getDB("thonk");
          } catch(UnknownHostException ex)  {
              log.severe(ex.getMessage());
          }
    }

    public String createCategory(Category cat) {

        DBCollection dbCollection = db.getCollection("categories");
        JacksonDBCollection<Category, String> coll
            = JacksonDBCollection.wrap(dbCollection, Category.class, String.class);
        WriteResult<Category, String> result = coll.insert(cat);
        return result.getSavedId();

    }


    public Category readCategory(String id) {

        DBCollection dbCollection = db.getCollection("categories");
        JacksonDBCollection<Category, String> coll
            = JacksonDBCollection.wrap(dbCollection, Category.class, String.class);
        Category cat = coll.findOneById(id);
        return cat;

    }

    public void updateParentCategory(String catId, String parentId) {

        DBCollection dbCollection = db.getCollection("categories");
        JacksonDBCollection<Category, String> coll 
            = JacksonDBCollection.wrap(dbCollection, Category.class, String.class);
        coll.updateById(catId, DBUpdate.set("parentCategory", parentId));

    }

    public void addChild(String catId, String childId) {

        DBCollection dbCollection = db.getCollection("categories");
        JacksonDBCollection<Category, String> coll 
            = JacksonDBCollection.wrap(dbCollection, Category.class, String.class);
        Child child = new Child();
        child.categoryId = childId;
        child.parentId = catId;
        coll.updateById(catId, DBUpdate.inc("children").push("children", child));

    }

    public void addRelated(String catId, String relatedId, Double index) {

        DBCollection dbCollection = db.getCollection("categories");
        JacksonDBCollection<Category, String> coll 
            = JacksonDBCollection.wrap(dbCollection, Category.class, String.class);
        Related relative = new Related();
        relative.categoryId = catId;
        relative.relationIndex = index.toString();
        coll.updateById(catId, DBUpdate.inc("related").push("related", relative));

    }

    public void addPaper(String catId, String url) {

        DBCollection dbCollection = db.getCollection("categories");
        JacksonDBCollection<Category, String> coll 
            = JacksonDBCollection.wrap(dbCollection, Category.class, String.class);
        Paper paper  = new Paper();
        paper.url = url;
        coll.updateById(catId, DBUpdate.inc("papers").push("papers", paper));

    }


    public List<Category> getChildCategories(String catId) {

        List<Category> cats = new ArrayList<>();
        DBCollection dbCollection = db.getCollection("categories");
        JacksonDBCollection<Category, String> coll 
            = JacksonDBCollection.wrap(dbCollection, Category.class, String.class);
        Category cat = coll.findOneById(catId);
        for (DBRef<Child, String> child : cat.children) {
            Category chitlinCat = coll.findOneById(child.fetch().categoryId);
            cats.add(chitlinCat);
        }
        return cats;

    }

    public List<Category> getRelatedCategories(String catId) {

        List<Category> cats = new ArrayList<>();
        DBCollection dbCollection = db.getCollection("categories");
        JacksonDBCollection<Category, String> coll
            = JacksonDBCollection.wrap(dbCollection, Category.class, String.class);
        Category cat = coll.findOneById(catId);
        for (DBRef<Related, String> relative : cat.related) {
            Category sissyCat = coll.findOneById(relative.fetch().categoryId);
            cats.add(sissyCat);
        }
        return cats;
    }

    public List<Paper> getPapers(String catId) {

        DBCollection dbCollection = db.getCollection("categories");
        JacksonDBCollection<Category, String> coll
            = JacksonDBCollection.wrap(dbCollection, Category.class, String.class);
        Category cat = coll.findOneById(catId);
        List<Paper> papers = coll.fetch(cat.papers);
        return papers;

    }

    public void deleteCategory(String id) {

        DBCollection dbCollection = db.getCollection("categories");
        JacksonDBCollection<Category, String> coll
            = JacksonDBCollection.wrap(dbCollection, Category.class, String.class);
        coll.removeById(id);

    }

}
