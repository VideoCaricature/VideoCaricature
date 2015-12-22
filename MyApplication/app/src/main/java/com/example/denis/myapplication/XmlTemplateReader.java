package com.example.denis.myapplication;

import android.content.Context;
import android.graphics.BitmapFactory;
import android.graphics.PointF;
import android.util.Log;
import org.xmlpull.v1.XmlPullParser;
import java.util.ArrayList;
import java.util.List;

public class XmlTemplateReader {
    private static class Point{
        TemplateElement.LandmarkType type;
        int x;
        int y;
        Point(String str, String px, String py){
            type = TemplateElement.LandmarkType.valueOf(str);
            x = Integer.parseInt(px);
            y = Integer.parseInt(py);
        }
    };
    private static class Element{
        String name;
        int width;
        int height;
        List<Point> point;
        Element(String str, String w, String h){
            name = str;
            width = Integer.parseInt(w);
            height = Integer.parseInt(h);
            point = new ArrayList<>();
        }
        void addPoint(Point p){
            point.add(p);
        }
    };

    public XmlTemplateReader(Context ctx, String str){
        name = str;
        context = ctx;
        drawer = new TemplateDrawer(str);
        elements = new ArrayList<>();
    }

    boolean parse(){
        try{
            int i = 0;
            XmlPullParser parser = context.getResources().getXml(R.xml.class.getField(name.toLowerCase()).getInt(null));
            while (parser.getEventType() != XmlPullParser.END_DOCUMENT){
                if (parser.getEventType() == XmlPullParser.START_TAG && parser.getName().equals("element"))
                    elements.add(new Element(parser.getAttributeValue(0), parser.getAttributeValue(1), parser.getAttributeValue(2)));
                if (parser.getEventType() == XmlPullParser.START_TAG && parser.getName().equals("point"))
                    elements.get(i).addPoint(new Point(parser.getAttributeValue(0), parser.getAttributeValue(1), parser.getAttributeValue(2)));
                if (parser.getEventType() == XmlPullParser.END_TAG && parser.getName().equals("element"))
                    i++;
                parser.next();
            }
            return true;
        }
        catch(Exception e){
            Log.e("xmlParser","Failure to get template element.", e);
        }
        return false;
    }

    TemplateDrawer getElement(){
        if (parse()){
            for (int i = 0; i < elements.size(); i++){
                TemplateElement tmplElement = new TemplateElement();
                try{
                    tmplElement.setBitmap(BitmapFactory.decodeResource(context.getResources(), R.drawable.class.getField(elements.get(i).name).getInt(null)));
                }
                catch (Exception e){
                    Log.e("xmlParser", "Failure to get drawable id.", e);
                }
                tmplElement.setBitmapSize(elements.get(i).width, elements.get(i).height);
                for (int j = 0; j < elements.get(i).point.size(); j++)
                    tmplElement.addLandmark(elements.get(i).point.get(j).type, new PointF(elements.get(i).point.get(j).x, elements.get(i).point.get(j).y));
                drawer.addElement(tmplElement);
            }
        }
        else throw new Error("Failure of parse");
        return drawer;
    }

    TemplateDrawer drawer;
    String name;
    Context context;
    List<Element> elements;
}
