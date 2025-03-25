package com.example.addressBook.Interface;

import com.example.addressBook.dto.ContactDTO;
import java.util.List;

public interface IAddressBookService {
    List<ContactDTO> getAllContacts();
    ContactDTO getContactById(Long id);
    ContactDTO saveContact(ContactDTO contactDTO);
    ContactDTO updateContact(Long id, ContactDTO contactDTO);
    boolean deleteContact(Long id);
}
