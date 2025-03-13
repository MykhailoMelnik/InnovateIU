import lombok.Builder;
import lombok.Data;

import java.time.Instant;
import java.util.*;
import java.util.stream.Collectors;

/**
 * For implement this task focus on clear code, and make this solution as simple readable as possible
 * Don't worry about performance, concurrency, etc
 * You can use in Memory collection for sore data
 * <p>
 * Please, don't change class name, and signature for methods save, search, findById
 * Implementations should be in a single class
 * This class could be auto tested
 */
public class DocumentManager {

    private final Map<String, Document> storage = new HashMap<>();

    /**
     * Implementation of this method should upsert the document to your storage
     * And generate unique id if it does not exist, don't change [created] field
     *
     * @param document - document content and author data
     * @return saved document
     */
    public Document save(Document document) {
        if (document.getId() == null) {
            document.setId(UUID.randomUUID().toString());
        }
        if (storage.containsKey(document.getId())) {
            Document existing = storage.get(document.getId());
            document.setCreated(existing.getCreated());
        } else {
            if (document.getCreated() == null) {
                document.setCreated(Instant.now());
            }
        }
        storage.put(document.getId(), document);
        return document;
    }

    /**
     * Implementation this method should find documents which match with request
     *
     * @param request - search request, each field could be null
     * @return list matched documents
     */
    public List<Document> search(SearchRequest request) {
        return storage.values().stream()
                .filter(doc -> matchTitle(doc, request.getTitlePrefixes()))
                .filter(doc -> matchContent(doc, request.getContainsContents()))
                .filter(doc -> matchAuthor(doc, request.getAuthorIds()))
                .filter(doc -> matchCreated(doc, request.getCreatedFrom(), request.getCreatedTo()))
                .collect(Collectors.toList());
    }

    /**
     * Implementation this method should find document by id
     *
     * @param id - document id
     * @return optional document
     */
    public Optional<Document> findById(String id) {
        return Optional.ofNullable(storage.get(id));
    }

    private boolean matchTitle(Document doc, List<String> prefixes) {
        if (prefixes == null || prefixes.isEmpty()) return true;
        return prefixes.stream().anyMatch(prefix -> doc.getTitle().startsWith(prefix));
    }

    private boolean matchContent(Document doc, List<String> contents) {
        if (contents == null || contents.isEmpty()) return true;
        return contents.stream().anyMatch(content -> doc.getContent().contains(content));
    }

    private boolean matchAuthor(Document doc, List<String> authorIds) {
        if (authorIds == null || authorIds.isEmpty()) return true;
        return authorIds.contains(doc.getAuthor().getId());
    }

    private boolean matchCreated(Document doc, Instant from, Instant to) {
        if (from != null && doc.getCreated().isBefore(from)) return false;
        if (to != null && doc.getCreated().isAfter(to)) return false;
        return true;
    }


    @Data
    @Builder
    public static class SearchRequest {
        private List<String> titlePrefixes;
        private List<String> containsContents;
        private List<String> authorIds;
        private Instant createdFrom;
        private Instant createdTo;
    }

    @Data
    @Builder
    public static class Document {
        private String id;
        private String title;
        private String content;
        private Author author;
        private Instant created;
    }

    @Data
    @Builder
    public static class Author {
        private String id;
        private String name;
    }
}
