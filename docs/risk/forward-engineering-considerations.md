** Forward Engineering Considerations**

These seven concerns are linked to current requirements and risks.
FEC-001 is linked to RISK-003 and RISK-004 (security); FEC-002 is linked
to NFR acceptance criteria in requirements; FEC-003 is linked to
RISK-006 (hosting limits) and DEC-003 (stack deferred); FEC-004 is
linked to persistence design in M2; FEC-005 is linked to M3 release
readiness; FEC-006 is linked to performance NFR; FEC-007 is linked to
DEC-004 (architecture deferred to M2).

  ----------------------------------------------------------------------------------------------------
  ID        Concern             Why It Matters  Later               Information    Risk of Ignoring 
                                Now             Decision/Activity   Still Missing  It               
                                                It Influences                                       
  --------- ------------------- --------------- ------------------- -------------- ---------------- --
  FEC-001   **Authentication &  CivicConnect    M2 architecture and What personal  If ignored now,  
            Authorisation**     stores service  technology          data each role access rules     
                                requests that   selection. The      can access.    will be bolted   
                                may contain     choice of           Whether any    on late. This    
                                sensitive data  authentication      single sign-on creates security 
                                (e.g. security  method (            or external    gaps and         
                                concerns,       session-based,      identity       requires         
                                personal        token-based)        provider is    expensive rework 
                                details). The   affects how the API required.      across the       
                                system has      is structured and                  codebase.        
                                three distinct  how roles are                                       
                                user roles with enforced throughout                                 
                                different       the system.                                         
                                access rights.                                                      
                                These rules                                                         
                                must be                                                             
                                captured in                                                         
                                requirements                                                        
                                before any                                                          
                                design begins.                                                      

  FEC-002   **Testability of    M3 requires     M2 and M3: test     Confirmation   If requirements  
            Requirements**      automated tests strategy, test      that all       are untestable,  
                                traceable to    tooling selection,  functional     the team cannot  
                                requirements.   and CI pipeline.    requirements   produce credible 
                                If requirements Vague requirements  have           quality evidence 
                                are written     produce vague       measurable     in M3. This      
                                without         tests, which        acceptance     affects both the 
                                measurable      produce no useful   criteria. Team product quality  
                                acceptance      quality evidence.   agreement on   and the          
                                criteria, there                     what           assessment       
                                is nothing to                       constitutes a  result.          
                                test against.                       passing test.                   
                                Requirements                                                        
                                that cannot be                                                      
                                tested cannot                                                       
                                be verified.                                                        

  FEC-003   **Deployment        The project     M2 technology       Which          If no            
            Environment &       must stay       selection and M3    free-tier      environment is   
            Hosting Limits**    within free or  staging deployment. platforms      identified in    
                                low-cost        The chosen stack    support the    M2, M3 staging   
                                services.       must be compatible  candidate      deployment may   
                                Different       with the available  technology     fail late. The   
                                hosting         hosting             stacks. What   team could be    
                                platforms have  environment.        the connection forced to        
                                different                           and data       migrate under    
                                limits on                           limits are for time pressure.   
                                database size,                      each option.                    
                                request volume,                                                     
                                and build time.                                                     
                                These limits                                                        
                                affect what the                                                     
                                system can                                                          
                                realistically                                                       
                                do.                                                                 

  FEC-004   **Data Recovery &   Service         M2 persistence      How long       Without a backup 
            Request History     requests are    design (database    request        or recovery      
            Preservation**      operational     choice, backup      history must   strategy, a      
                                records for the strategy). M4       be retained.   database failure 
                                organisation.   operational         Whether the    in production    
                                If data is lost readiness review    organisation   means permanent  
                                due to a system must confirm that a has any        loss of all      
                                failure, there  recovery path       existing data  request records. 
                                is no paper     exists.             to migrate     This undermines  
                                backup to fall                      into the new   the core         
                                back on. The                        system.        business value   
                                system replaces                                    of the system.   
                                the current                                                         
                                process                                                             
                                entirely, so                                                        
                                data loss has a                                                     
                                direct business                                                     
                                impact.                                                             

  FEC-005   **Observability:    When the system M3 release          What events    A production     
            Logging & Error     is in use,      readiness and M4    should be      failure with no  
            Visibility**        errors will     operational         logged         logs cannot be   
                                occur. Without  evidence. The       (errors,       diagnosed        
                                any logging,    assessors will ask  status         quickly. This    
                                the team has no how the team knows  changes,       creates downtime 
                                way to know     the system is       failed         and gives the    
                                what went       working in          logins).       organisation no  
                                wrong, where,   production beyond   Whether any    accountability   
                                or why.         \"the page          logging        trail for what   
                                                loaded\".           service is     happened to      
                                                                    available      their service    
                                                                    within the     requests.        
                                                                    free-tier                       
                                                                    budget.                         

  FEC-006   **Concurrent User   The system must M2 architecture     The expected   If concurrency   
            Load &              handle multiple decisions (database number of      is not           
            Performance**       requesters      engine, server      simultaneous   considered until 
                                submitting and  model). An NFR for  users. Whether M3, the          
                                checking        response time must  the free-tier  architecture may 
                                requests at the be defined in M1 to hosting        already be       
                                same time,      guide these         supports       fixed.           
                                while staff are decisions.          enough         Retrofitting     
                                also updating                       concurrent     scalability is   
                                statuses. If                        connections.   significantly    
                                the database or                                    harder and more  
                                server cannot                                      expensive than   
                                handle this,                                       designing for it 
                                the system will                                    early.           
                                slow down or                                                        
                                crash under                                                         
                                real use.                                                           

  FEC-007   **Maintainability & The project     M2 design decisions Team agreement Without          
            Code Structure**    runs across     (layering, module   on a coding    consistent       
                                four            structure). M3      standard or    structure,       
                                milestones.     change request      structural     different team   
                                Code written    implementation. M4  approach (to   members produce  
                                without agreed  technical debt      be decided in  incompatible     
                                structure in M3 reflection.         M2, not M1).   code.            
                                becomes harder                      This is        Integration      
                                to change in M4                     recorded here  becomes slower,  
                                and beyond.                         so the         and more errors  
                                Technical debt                      decision is    occur as the     
                                introduced                          not forgotten. codebase grows.  
                                early reduces                                                       
                                the team\'s                                                         
                                ability to                                                          
                                respond to the                                                      
                                M3 change                                                           
                                request.                                                            
  ----------------------------------------------------------------------------------------------------
