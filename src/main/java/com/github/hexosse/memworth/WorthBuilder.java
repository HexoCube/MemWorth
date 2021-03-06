/*
 * Copyright 2015 Hexosse
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.github.hexosse.memworth;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVRecord;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.text.NumberFormat;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Locale;

/**
 * This file is part of MemWorth
 *
 * @author <b>hexosse</b> (<a href="https://github.com/hexosse">hexosse on GitHub</a>).
 */
public class WorthBuilder
{
    private File csvFile;
    private CSVFormat csvFormat = CSVFormat.EXCEL.withDelimiter(';').withIgnoreEmptyLines(true);
    private NumberFormat numFormat = NumberFormat.getInstance(Locale.getDefault());
    private ArrayList<Item> items = new ArrayList<Item>();
    private StringBuilder yamlSb = new StringBuilder();
    private String ls = System.getProperty("line.separator");
    private String indent = "  ";


    /**
     * @param csvFile csv file to parse
     */
    public WorthBuilder(String csvFile)
    {
        this.csvFile = new File(csvFile);
    }


    /**
     * Parse the csv file
     *
     * @return WorthBuilder
     */
     public WorthBuilder parse()
    {
        try
        {
            // Free list of items
            items.clear();

            // Load csv file into file reader
            FileReader reader = new FileReader(csvFile);

            // parse csv file using specified file format
            for(CSVRecord record : csvFormat.parse(reader))
            {
                // Skip empty lines
                if (record.get(0).isEmpty())    continue;
                // Skip title
                if (record.get(0).equals("ID")) continue;
                // Skip item without price
                if (record.get(3).isEmpty())    continue;
                if (record.get(4).isEmpty())    continue;

                // Create Item
                String[] idData = record.get(0).replaceAll("[^0-9]"," ").trim().split(" ");
                int id = Integer.parseInt(idData[0]);
                int data = Integer.parseInt(idData.length > 1 ? idData[1] : "0");
                String comment = record.get(1);
                String sworth = record.get(4).trim().replaceAll(" ","").replaceAll("^[^a-zA-Z0-9\\s]+|[^a-zA-Z0-9\\s]+$", "");
                Number worth = numFormat.parse(sworth);

                // Add Item to list
                if(worth.doubleValue()!=0)
                    items.add(new Item(id,data,comment,worth.doubleValue()));
            }

            // Close csv file
            reader.close();

            // Sort the item list
            Collections.sort(items);

        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (ParseException e) {
            e.printStackTrace();
        }

        return this;
    }

    private void addLine(String text)
    {
        yamlSb.append(text).append(ls);
    }

    public String toYaml(boolean useName)
    {
        // Clear StringBuilder
        yamlSb = new StringBuilder();

        // First lines
        addLine("# ");
        addLine("# Worth file generated by MemWorth");
        addLine("# ");
        addLine("# hexosse ®");
        addLine("# ");
        addLine("");
        addLine("worth:");

        // Loop on all items
        for(int i=0;i<items.size();i++)
        {
            Item prevItem = (i>0)?items.get(i-1):null;
            Item item = items.get(i);
            Item nextItem = (i<items.size()-1)?items.get(i+1):null;

            // Items without data value
            if(!item.hasSameId(nextItem) && !item.hasSameId(prevItem))
            {
                addLine(indent + "# " + item.getComment());
                addLine(indent + (useName?item.getName():item.getId()) + ": " + item.getWorth());
            }

            // first item with data value
            else if(item.hasSameId(nextItem) && !item.hasSameId(prevItem))
            {
                addLine(indent + "# " + item.getComment());
                addLine(indent + (useName?item.getName():item.getId()) + ":");
                addLine(indent + indent + "# " + item.getComment());
                addLine(indent + indent + "'" + item.getData() + "': " + item.getWorth());
            }

            // next item with data value
            else if(item.hasSameId(prevItem))
            {
                addLine(indent + indent + "# " + item.getComment());
                addLine(indent + indent + "'" + item.getData() + "': " + item.getWorth());
            }
        }

        return yamlSb.toString();
    }

    public File toFile()
    {
        return null;
    }
}
