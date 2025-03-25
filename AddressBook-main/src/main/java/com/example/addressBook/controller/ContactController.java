package com.example.addressBook.controller;

import com.example.addressBook.dto.ContactDTO;
import com.example.addressBook.dto.ResponseDTO;
import com.example.addressBook.service.ContactService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/contacts")
public class ContactController {

    @Autowired
    private ContactService contactService;

    // ✅ GET: Fetch all contacts
    @GetMapping
    public ResponseEntity<ResponseDTO<List<ContactDTO>>> getAllContacts() {
        List<ContactDTO> contactDTOs = contactService.getAllContacts();
        return ResponseEntity.ok(new ResponseDTO<>("All contacts fetched successfully", contactDTOs));
    }

    // ✅ GET: Fetch contact by ID
    @GetMapping("/{id}")
    public ResponseEntity<ResponseDTO<ContactDTO>> getContactById(@PathVariable Long id) {
        ContactDTO contact = contactService.getContactById(id);

        if (contact != null) {
            return ResponseEntity.ok(new ResponseDTO<>("Contact found", contact));
        } else {
            return ResponseEntity.status(404).body(new ResponseDTO<>("Contact not found", null));
        }
    }

    // ✅ POST: Add a new contact
    @PostMapping
    public ResponseEntity<ResponseDTO<ContactDTO>> addContact(@RequestBody ContactDTO contactDTO) {
        ContactDTO savedContact = contactService.saveContact(contactDTO);
        return ResponseEntity.status(201).body(new ResponseDTO<>("Contact added successfully", savedContact));
    }

    // ✅ PUT: Update a contact
    @PutMapping("/{id}")
    public ResponseEntity<ResponseDTO<ContactDTO>> updateContact(@PathVariable Long id, @RequestBody ContactDTO contactDTO) {
        ContactDTO updatedContact = contactService.updateContact(id, contactDTO);

        if (updatedContact != null) {
            return ResponseEntity.ok(new ResponseDTO<>("Contact updated successfully", updatedContact));
        } else {
            return ResponseEntity.status(404).body(new ResponseDTO<>("Contact not found", null));
        }
    }

    // ✅ DELETE: Delete a contact
    @DeleteMapping("/{id}")
    public ResponseEntity<ResponseDTO<String>> deleteContact(@PathVariable Long id) {
        boolean deleted = contactService.deleteContact(id);
        if (deleted) {
            return ResponseEntity.ok(new ResponseDTO<>("Contact deleted successfully", "ID: " + id));
        } else {
            return ResponseEntity.status(404).body(new ResponseDTO<>("Contact not found", null));
        }
    }
}
