package org.hipeoplea.Ib_lr1.data;

import java.util.List;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.util.HtmlUtils;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/api")
public class ApiDataController {

    private final ApiDataRepository dataRepository;

    public ApiDataController(ApiDataRepository dataRepository) {
        this.dataRepository = dataRepository;
    }

    @GetMapping("/data")
    public List<ApiDataItem> getData() {
        return dataRepository.findData().stream()
                .map(item -> new ApiDataItem(
                        item.id(),
                        HtmlUtils.htmlEscape(item.title()),
                        HtmlUtils.htmlEscape(item.content())))
                .toList();
    }

    @PostMapping("/notes")
    public ResponseEntity<NoteResponse> createNote(
            @AuthenticationPrincipal Jwt jwt,
            @Valid @RequestBody NoteRequest request) {
        NoteRecord note = dataRepository.createNote(jwt.getSubject(), request.title(), request.content());
        return ResponseEntity.status(HttpStatus.CREATED).body(toResponse(note));
    }

    @GetMapping("/notes")
    public List<NoteResponse> getMyNotes(@AuthenticationPrincipal Jwt jwt) {
        return dataRepository.findNotesByOwner(jwt.getSubject()).stream()
                .map(ApiDataController::toResponse)
                .toList();
    }

    private static NoteResponse toResponse(NoteRecord note) {
        return new NoteResponse(
                note.id(),
                HtmlUtils.htmlEscape(note.title()),
                HtmlUtils.htmlEscape(note.content()),
                note.createdAt());
    }
}
