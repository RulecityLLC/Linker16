package com.rulecity;

import com.rulecity.aggregation.OMFFile;
import com.rulecity.aggregation.OMFFileImpl;
import com.rulecity.parse.OMFItem;
import com.rulecity.parse.OMFParser;
import com.rulecity.parse.OMFParserImpl;

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

        OMFParser parserObj = new OMFParserImpl();
        List<OMFItem> objItems = parserObj.parseBinary(data);

        for (OMFItem item : objItems)
        {
            System.out.println(item.getTypeString() + ":");
            System.out.println(item.getDataString());
        }

        OMFFile fileObj = new OMFFileImpl(objItems);
    }
}