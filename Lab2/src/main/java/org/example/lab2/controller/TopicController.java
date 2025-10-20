package org.example.lab2.controller;

import lombok.RequiredArgsConstructor;
import org.example.lab2.entity.Topic;
import org.example.lab2.service.TopicService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Controller
@RequestMapping
@RequiredArgsConstructor
public class TopicController {

    private final TopicService topicService;

    @GetMapping("/")
    public String root() {
        return "redirect:/topics";
    }

    @GetMapping("/topics")
    public String list(Model model) {
        List<Topic> topics = topicService.findAll();
        model.addAttribute("topics", topics);
        return "topics/list";
    }

    @GetMapping("/topics/{id}")
    public String detail(@PathVariable UUID id, Model model) {
        topicService.incrementViewCount(id);
        model.addAttribute("topic", topicService.getById(id));
        model.addAttribute("posts", List.of());
        return "topics/detail";
    }

    @PostMapping("/topics/{id}/posts")
    public String addPost(@PathVariable UUID id,
                          @RequestParam String content,
                          @RequestParam String author) {
        topicService.touchReply(id, LocalDateTime.now());
        return "redirect:/topics/{id}";
    }

    @GetMapping("/admin/topics/new")
    public String newForm(Model model) {
        model.addAttribute("topic", new Topic());
        model.addAttribute("tagsRaw", "");
        return "topics/form";
    }

    @PostMapping("/admin/topics")
    public String create(@RequestParam String title,
                         @RequestParam(required = false) String description,
                         @RequestParam(required = false, defaultValue = "false") boolean pinned,
                         @RequestParam(required = false, defaultValue = "false") boolean closed,
                         @RequestParam(required = false, name = "tagsRaw") String tagsRaw) {

        Topic t = new Topic();
        t.setTitle(title);
        t.setDescription(description);
        t.setAuthor("admin"); // для демо
        t.setPinned(pinned);
        t.setClosed(closed);
        t.setTags(parseTags(tagsRaw));
        t.setDeleted(false);
        t.setReplyCount(0);
        t.setViewCount(0);
        t.setCreatedAt(LocalDateTime.now());
        t.setUpdatedAt(LocalDateTime.now());

        Topic saved = topicService.save(t);
        return "redirect:/topics/" + saved.getId();
    }

    @GetMapping("/admin/topics/{id}/edit")
    public String editForm(@PathVariable UUID id, Model model) {
        Topic topic = topicService.getById(id);
        model.addAttribute("topic", topic);
        model.addAttribute("tagsRaw", joinTags(topic.getTags()));
        return "topics/form";
    }

    @PostMapping("/admin/topics/{id}")
    public String update(@PathVariable UUID id,
                         @RequestParam String title,
                         @RequestParam(required = false) String description,
                         @RequestParam(required = false, defaultValue = "false") boolean pinned,
                         @RequestParam(required = false, defaultValue = "false") boolean closed,
                         @RequestParam(required = false, name = "tagsRaw") String tagsRaw) {

        Topic topic = topicService.getById(id);
        topic.setTitle(title);
        topic.setDescription(description);
        topic.setPinned(pinned);
        topic.setClosed(closed);
        topic.setTags(parseTags(tagsRaw));
        topic.setUpdatedAt(LocalDateTime.now());
        topicService.update(topic);

        return "redirect:/topics/" + id;
    }

    @PostMapping("/admin/topics/{id}/delete")
    public String delete(@PathVariable UUID id) {
        topicService.deleteById(id);
        return "redirect:/topics";
    }


    private static Set<String> parseTags(String raw) {
        if (raw == null || raw.isBlank()) return Set.of();
        return Stream.of(raw.split(","))
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .collect(Collectors.toSet());
    }

    private static String joinTags(Set<String> tags) {
        if (tags == null || tags.isEmpty()) return "";
        return String.join(", ", tags);
    }
}
