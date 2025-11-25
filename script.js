document.addEventListener('DOMContentLoaded', () => {
    // Initialize Lucide Icons
    lucide.createIcons();

    // Accordion Functionality
    const accordionBtns = document.querySelectorAll('.accordion-btn');

    accordionBtns.forEach(btn => {
        btn.addEventListener('click', () => {
            const content = btn.nextElementSibling;
            const isExpanded = btn.getAttribute('aria-expanded') === 'true';

            // Close all other accordions
            accordionBtns.forEach(otherBtn => {
                if (otherBtn !== btn) {
                    otherBtn.setAttribute('aria-expanded', 'false');
                    otherBtn.nextElementSibling.classList.remove('open');
                    otherBtn.nextElementSibling.style.maxHeight = null;
                }
            });

            // Toggle current accordion
            btn.setAttribute('aria-expanded', !isExpanded);
            content.classList.toggle('open');
            
            if (!isExpanded) {
                content.style.maxHeight = content.scrollHeight + "px";
            } else {
                content.style.maxHeight = null;
            }
        });
    });

    // Mobile Menu (Optional - if we add one later)
    // Currently the design hides the nav on mobile but doesn't show a hamburger menu in the original code.
    // If needed, we can add it here.
});
