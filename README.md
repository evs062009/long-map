# long-map

Finish development of class LongMapImpl, which implements a map with keys of type long. It has to be a hash table (like HashMap). Requirements:
* it should not use any known Map implementations; 
* it should use as less memory as possible and have adequate performance;
* the main aim is to see your codestyle and teststyle 

# Solution
The implementation is based on open addressing principle with double hashing 
(a version using cuckoo hashing was planned, but has not yet been implemented). 
Data storage (key-value Entry) are a "main table" (an array) and a "reserve" 
(a manually implemented linked list). The reserve guarantees entry saving before 
max size achieving. Also, it reduces the number of needed rehashing. The size of 
the reserve is limited to 5% of the total number of stored in the map elements.

It is assumed that the economy of memory is achieved by:
* Use of open addressing - saving on the absence of links in Entry (except for those 
    stored in the reserve, but this quantity is not more than 5% of the total);
* Automatic trim when the bottom load factor is reached - saving on the number of 
    unnecessary (empty) cells.
* When the map is empty, the main array and reserve are deleted (are null) and lazy 
    initialization of the main array - saving on unused maps.
* /planned, but not implemented/ Using maps with a smaller format (int, short, byte) 
    inside the main map. If the key allows, the element is stored in the next level 
    map (int instead long, cascading like a nested doll) - saving on the size of the 
    primitive for keys storage.