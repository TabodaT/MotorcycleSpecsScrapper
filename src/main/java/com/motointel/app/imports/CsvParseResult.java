package com.motointel.app.imports;

import com.motointel.app.dto.ImportResultDto;

import java.util.List;

/** Outcome of parsing a CSV upload: valid rows plus per-row validation errors. */
public record CsvParseResult(List<ParsedRow> rows, List<ImportResultDto.RowError> errors) {
}
