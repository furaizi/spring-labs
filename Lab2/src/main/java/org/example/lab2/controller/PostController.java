package org.example.lab2.controller;

import org.example.lab2.entity.Post;
import org.example.lab2.service.PostService;
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

    @Autowired
    public PostController(PostService postService) {
        this.postService = postService;
    }

    @GetMapping
    public String showSearchPage() {
        return "posts/search-home";
    }

    @GetMapping("/by-topic")
    public String listPostsByTopic(@RequestParam("id") UUID topicId, Model model) {
        model.addAttribute("posts", postService.getPostsByTopicId(topicId));
        model.addAttribute("listTitle", "Пости за темою: " + topicId);
        return "posts/list";
    }

    @GetMapping("/by-author")
    public String listPostsByAuthor(@RequestParam("id") UUID authorId, Model model) {
        model.addAttribute("posts", postService.getPostsByAuthorId(authorId));
        model.addAttribute("listTitle", "Пости автора: " + authorId);
        return "posts/list";
    }

    @GetMapping("/{id}")
    public String viewPost(@PathVariable UUID id, Model model) {
        Optional<Post> post = postService.getPostById(id);
        if (post.isPresent()) {
            model.addAttribute("post", post.get());
            return "posts/view";
        } else {
            return "error/404";
        }
    }

    @GetMapping("/new")
    public String showCreateForm(Model model) {
        model.addAttribute("post", new Post());
        return "posts/create-form";
    }

    @PostMapping("/new")
    public String createPost(@ModelAttribute Post post) {
        if (post.getAuthorId() == null) {
            post.setAuthorId(UUID.randomUUID());
        }
        if (post.getTopicId() == null) {
            post.setTopicId(UUID.randomUUID());
        }

        Post createdPost = postService.createPost(post);
        return "redirect:/posts/" + createdPost.getId();
    }

    @GetMapping("/{id}/edit")
    public String showEditForm(@PathVariable UUID id, Model model) {
        Optional<Post> post = postService.getPostById(id);
        if (post.isPresent()) {
            model.addAttribute("post", post.get());
            return "posts/edit-form";
        } else {
            return "error/404";
        }
    }

    @PostMapping("/{id}/edit")
    public String updatePost(@PathVariable UUID id,
                             @RequestParam String title,
                             @RequestParam String content) {
        postService.updatePost(id, title, content);
        return "redirect:/posts/" + id;
    }

    @PostMapping("/{id}/delete")
    public String deletePost(@PathVariable UUID id) {
        postService.deletePost(id);
        return "redirect:/posts";
    }

    @PostMapping("/{id}/like")
    public String likePost(@PathVariable UUID id) {
        postService.likePost(id);
        return "redirect:/posts/" + id;
    }

    @PostMapping("/{id}/unlike")
    public String unlikePost(@PathVariable UUID id) {
        postService.unlikePost(id);
        return "redirect:/posts/" + id;
    }

    @GetMapping("/search")
    public String searchPosts(@RequestParam String keyword, Model model) {
        model.addAttribute("posts", postService.getPostByTitle(keyword));
        return "posts/list";
    }
}