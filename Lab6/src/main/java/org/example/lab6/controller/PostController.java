package org.example.lab6.controller;

import org.example.lab6.entity.Post;
import org.example.lab6.service.PostService;
import org.example.lab6.service.TopicService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;
import java.util.UUID;

@Controller
@RequestMapping("/posts")
public class PostController {

    private final PostService postService;
    private final TopicService topicService;

    @Autowired
    public PostController(PostService postService, TopicService topicService) {
        this.postService = postService;
        this.topicService = topicService;
    }

    @GetMapping
    public String showSearchPage() {
        return "posts/search-home";
    }

    @GetMapping("/by-topic")
    public String listPostsByTopic(@RequestParam("id") UUID topicId, Model model) {
        model.addAttribute("posts", postService.getPostsByTopicId(topicId));
        model.addAttribute("listTitle", "Пости за темою: " + topicId);
        model.addAttribute("backToTopicId", topicId);
        return "posts/list";
    }

    @GetMapping("/by-author")
    public String listPostsByAuthor(@RequestParam("id") UUID authorId, Model model) {
        model.addAttribute("posts", postService.getPostsByAuthorId(authorId));
        model.addAttribute("listTitle", "Пости автора: " + authorId);
        return "posts/list";
    }

    @GetMapping("/{id}")
    public String viewPost(@PathVariable UUID id,
                           @RequestParam(value = "backToTopicId", required = false) UUID backToTopicId,
                           Model model) {
        Optional<Post> post = postService.getPostById(id);
        if (post.isPresent()) {
            model.addAttribute("post", post.get());
            model.addAttribute("backToTopicId", backToTopicId);
            return "posts/view";
        } else {
            return "error/404";
        }
    }

    @GetMapping("/new")
    public String showCreateForm(@RequestParam(value = "topicId", required = false) UUID topicId,
                                 Model model) {
        Post p = new Post();
        p.setTopicId(topicId);
        model.addAttribute("post", p);
        model.addAttribute("backToTopicId", topicId);
        return "posts/create-form";
    }

    @PostMapping("/new")
    public String createPost(@ModelAttribute Post post,
                             @RequestParam(value = "backToTopicId", required = false) UUID backToTopicId) {
        if (post.getAuthorId() == null) post.setAuthorId(UUID.randomUUID());
        if (post.getTopicId() == null) post.setTopicId(backToTopicId);

        UUID topicId = post.getTopicId() != null ? post.getTopicId() : backToTopicId;
        Post createdPost = (topicId != null)
                ? topicService.addPostToTopic(topicId, post)
                : postService.createPost(post);

        if (topicId != null) {
            return "redirect:/topics/" + topicId;
        }
        return "redirect:/posts/" + createdPost.getId();
    }

    @GetMapping("/{id}/edit")
    public String showEditForm(@PathVariable UUID id,
                               @RequestParam(value = "backToTopicId", required = false) UUID backToTopicId,
                               Model model) {
        Optional<Post> post = postService.getPostById(id);
        if (post.isPresent()) {
            model.addAttribute("post", post.get());
            model.addAttribute("backToTopicId", backToTopicId);
            return "posts/edit-form";
        } else {
            return "error/404";
        }
    }

    @PostMapping("/{id}/edit")
    public String updatePost(@PathVariable UUID id,
                             @RequestParam String title,
                             @RequestParam String content,
                             @RequestParam(value = "backToTopicId", required = false) UUID backToTopicId) {
        postService.updatePost(id, title, content);
        return "redirect:/posts/" + id + (backToTopicId != null ? "?backToTopicId=" + backToTopicId : "");
    }

    @PostMapping("/{id}/delete")
    public String deletePost(@PathVariable UUID id,
                             @RequestParam(value = "backToTopicId", required = false) UUID backToTopicId) {
        Optional<Post> p = postService.getPostById(id);
        UUID topicId = (p.isPresent() && p.get().getTopicId() != null) ? p.get().getTopicId() : backToTopicId;
        if (topicId != null) {
            topicService.deletePostFromTopic(topicId, id);
        } else {
            postService.deletePost(id);
        }
        if (topicId != null) return "redirect:/topics/" + topicId;
        return "redirect:/posts";
    }

    @PostMapping("/{id}/like")
    public String likePost(@PathVariable UUID id,
                           @RequestParam(value = "backToTopicId", required = false) UUID backToTopicId) {
        postService.likePost(id);
        return "redirect:/posts/" + id + (backToTopicId != null ? "?backToTopicId=" + backToTopicId : "");
    }

    @PostMapping("/{id}/unlike")
    public String unlikePost(@PathVariable UUID id,
                             @RequestParam(value = "backToTopicId", required = false) UUID backToTopicId) {
        postService.unlikePost(id);
        return "redirect:/posts/" + id + (backToTopicId != null ? "?backToTopicId=" + backToTopicId : "");
    }

    @GetMapping("/search")
    public String searchPosts(@RequestParam String keyword, Model model) {
        model.addAttribute("posts", postService.getPostByTitle(keyword));
        return "posts/list";
    }
}
