package com.zl.mjga.dto;

import static com.zl.mjga.utils.StringCaseUtils.convertCamelCaseToSnake;
import static org.jooq.impl.DSL.field;
import static org.jooq.impl.DSL.name;

import io.swagger.v3.oas.annotations.media.Schema;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import lombok.*;
import org.apache.commons.lang3.StringUtils;
import org.jooq.SortField;
import org.jooq.SortOrder;

@Data
@NoArgsConstructor
public class PageRequestDto {

  public static final String REGEX = "^[a-zA-Z][a-zA-Z0-9_]*$";

  public static final String COLON = ":";

  private long page;
  private long size;

  @Schema(description = "排序字段", example = "name:asc,age:desc", type = "string")
  private Map<String, Direction> sortBy = new HashMap<>();

  public PageRequestDto(int page, int size) {
    checkPageAndSize(page, size);
    this.page = page;
    this.size = size;
  }

  public PageRequestDto(int page, int size, Map<String, Direction> sortBy) {
    checkPageAndSize(page, size);
    this.page = page;
    this.size = size;
    this.sortBy = sortBy;
  }

  @AllArgsConstructor
  @Getter
  public enum Direction {
    ASC("ASC"),
    DESC("DESC");

    private final String keyword;

    public static Direction fromString(String value) {
      try {
        return Direction.valueOf(value.toUpperCase(Locale.US));
      } catch (Exception e) {
        throw new IllegalArgumentException(
            String.format(
                "Invalid value '%s' for orders given; Has to be either 'desc' or 'asc' (case"
                    + " insensitive)",
                value),
            e);
      }
    }
  }

  public static PageRequestDto of(int page, int size) {
    return new PageRequestDto(page, size);
  }

  public static PageRequestDto of(int page, int size, Map<String, Direction> sortBy) {
    return new PageRequestDto(page, size, sortBy);
  }

  public List<SortField<Object>> getSortFields() {
    List<SortField<Object>> sortFields =
        sortBy.entrySet().stream()
            .map(
                (entry) ->
                    field(name(convertCamelCaseToSnake(entry.getKey())))
                        .sort(SortOrder.valueOf(entry.getValue().getKeyword())))
            .toList();
    if (sortFields.isEmpty()) {
      return List.of(field(name("id")).sort(SortOrder.DESC));
    } else {
      return sortFields;
    }
  }

  private void checkPageAndSize(int page, int size) {
    if (page < 0) {
      throw new IllegalArgumentException("Page index must not be less than zero");
    }

    if (size < 1) {
      throw new IllegalArgumentException("Page size must not be less than one");
    }
  }

  public long getOffset() {
    if (page == 0) {
      return 0;
    } else {
      return (page - 1) * size;
    }
  }

  public void setSortBy(String sortBy) {
    this.sortBy = convertSortBy(sortBy);
  }

  private Map<String, Direction> convertSortBy(String sortBy) {
    Map<String, Direction> result = new HashMap<>();
    if (StringUtils.isEmpty(sortBy)) {
      return result;
    }
    for (String fieldSpaceDirection : sortBy.split(",")) {
      String[] fieldDirectionArray = fieldSpaceDirection.split(COLON);
      if (fieldDirectionArray.length != 2) {
        throw new IllegalArgumentException(
            String.format(
                "Invalid sortBy field format %s. The expect format is [col1 asc,col2 desc]",
                sortBy));
      }
      String field = fieldDirectionArray[0];
      if (!verifySortField(field)) {
        throw new IllegalArgumentException(
            String.format("Invalid Sort field %s. Sort field must match %s", sortBy, REGEX));
      }
      String direction = fieldDirectionArray[1];
      result.put(field, Direction.fromString(direction));
    }
    return result;
  }

  private static boolean verifySortField(String sortField) {
    Pattern pattern = Pattern.compile(REGEX);
    Matcher matcher = pattern.matcher(sortField);
    return matcher.matches();
  }
}
