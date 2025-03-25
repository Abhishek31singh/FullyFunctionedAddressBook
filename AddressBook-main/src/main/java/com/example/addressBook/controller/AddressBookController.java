package com.example.addressBook.controller;

import com.example.addressBook.dto.ContactDTO;
import com.example.addressBook.dto.ResponseDTO;
import com.example.addressBook.Interface.IAddressBookService;
import com.example.addressBook.service.JwtTokenService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@Tag(name = "Address Book API", description = "API for managing contacts in an address book")
@RestController
@RequestMapping("/api/addressbook")
public class AddressBookController {

    @Autowired
    private IAddressBookService addressBookService;

    @Autowired
    private JwtTokenService jwtTokenService;

    // ✅ Fetch all contacts (CORS enabled)
    @Operation(summary = "Fetch all contacts", description = "Retrieves all contacts stored in the address book")
    @CrossOrigin(origins = "http://localhost:4200") // Allow Angular frontend
    @GetMapping
    public ResponseEntity<ResponseDTO<List<ContactDTO>>> getAllContacts() {
        List<ContactDTO> contactDTOs = addressBookService.getAllContacts();
        return ResponseEntity.ok(new ResponseDTO<>("All contacts fetched successfully", contactDTOs));
    }

    // ✅ Fetch contact by ID
    @Operation(summary = "Fetch contact by ID", description = "Retrieves a contact using their unique ID")
    @CrossOrigin(origins = "http://localhost:4200")
    @GetMapping("/{id}")
    public ResponseEntity<ResponseDTO<ContactDTO>> getContactById(@PathVariable Long id) {
        Optional<ContactDTO> contactDTO = Optional.ofNullable(addressBookService.getContactById(id));
        return contactDTO.map(dto -> ResponseEntity.ok(new ResponseDTO<>("Contact found", dto)))
                .orElseGet(() -> ResponseEntity.status(404).body(new ResponseDTO<>("Contact not found", null)));
    }

    // ✅ Add a new contact
    @Operation(summary = "Add a new contact", description = "Creates a new contact in the address book")
    @CrossOrigin(origins = "http://localhost:4200")
    @PostMapping
    public ResponseEntity<ResponseDTO<ContactDTO>> addContact(@RequestBody ContactDTO contactDTO) {
        ContactDTO savedContactDTO = addressBookService.saveContact(contactDTO);
        return ResponseEntity.status(201).body(new ResponseDTO<>("Contact added successfully", savedContactDTO));
    }

    // ✅ Update an existing contact
    @Operation(summary = "Update a contact", description = "Updates contact details using their ID")
    @CrossOrigin(origins = "http://localhost:4200")
    @PutMapping("/{id}")
    public ResponseEntity<ResponseDTO<ContactDTO>> updateContact(@PathVariable Long id, @RequestBody ContactDTO contactDTO) {
        Optional<ContactDTO> updatedContactDTO = Optional.ofNullable(addressBookService.updateContact(id, contactDTO));
        return updatedContactDTO.map(dto -> ResponseEntity.ok(new ResponseDTO<>("Contact updated successfully", dto)))
                .orElseGet(() -> ResponseEntity.status(404).body(new ResponseDTO<>("Contact not found", null)));
    }

    // ✅ Delete a contact
    @Operation(summary = "Delete a contact", description = "Removes a contact using their ID")
    @CrossOrigin(origins = "http://localhost:4200")
    @DeleteMapping("/{id}")
    public ResponseEntity<ResponseDTO<String>> deleteContact(@PathVariable Long id) {
        boolean deleted = addressBookService.deleteContact(id);
        return deleted
                ? ResponseEntity.ok(new ResponseDTO<>("Contact deleted successfully", "ID: " + id))
                : ResponseEntity.status(404).body(new ResponseDTO<>("Contact not found", null));
    }

    // ✅ Utility method to get user ID from the token
    private Long getUserIdFromToken() {
        try {
            String token = getAuthorizationToken();
            return jwtTokenService.verifyToken(token);
        } catch (Exception e) {
            return null; // Handle cases where token is invalid
        }
    }

    // ✅ Extract the token from the Authorization header
    private String getAuthorizationToken() {
        Object credentials = SecurityContextHolder.getContext().getAuthentication().getCredentials();
        return (credentials != null) ? credentials.toString() : null;
    }
}
