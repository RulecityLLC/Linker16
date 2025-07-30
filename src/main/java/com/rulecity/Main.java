package com.rulecity;

import com.rulecity.parse.ObjItem;
import com.rulecity.parse.ObjParser;
import com.rulecity.parse.ObjParserImpl;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

public class Main
{
    public static void main(String[] args) throws IOException
    {
        if (args.length < 1) {
            System.out.println("Please provide the input file");
            System.exit(0);
        }

        Path path = Paths.get(args[0]);
        byte[] data = Files.readAllBytes(path);

        ObjParser parserObj = new ObjParserImpl();
        List<ObjItem> objItems = parserObj.parseBinary(data);

        for (ObjItem item : objItems)
        {
            System.out.println(item.getTypeString() + ":");
            System.out.println(item.getDataString());
        }
    }
}