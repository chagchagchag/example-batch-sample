package com.example.batch_sample.global.reader;

import java.util.LinkedList;
import java.util.List;
import org.springframework.batch.item.ItemReader;
import org.springframework.batch.item.NonTransientResourceException;
import org.springframework.batch.item.ParseException;
import org.springframework.batch.item.UnexpectedInputException;

public class LinkedListItemReader <T> implements ItemReader<T> {
  private final List<T> items;

  public LinkedListItemReader(List<T> items){
    this.items = new LinkedList<>();
  }

  @Override
  public T read()
      throws Exception, UnexpectedInputException, ParseException, NonTransientResourceException {
    if(!items.isEmpty()){
      return items.remove(0);
    }
    return null;
  }
}
